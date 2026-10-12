package be.carvoice.app.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.car.app.CarContext
import androidx.car.app.media.CarAudioRecord
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * CarAudioRecord PCM -> offline Vosk recognition; never creates a phone AudioRecord.
 * Uses Vosk's Apache-2.0 Java API and adapts its stream decoding pattern:
 * Copyright 2019 Alpha Cephei Inc.; see THIRD_PARTY_CODE.md and the Vosk license notice.
 * Source: alphacep/vosk-api, SpeechStreamService.java, commit 05adbfcc0df27a1535913c6accd4b7fc60ffd59d.
 */
class CarVoiceController(
    private val carContext: CarContext,
    private val onListening: (Boolean) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val main = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor { work -> Thread(work, "CarVoiceOffline").apply { isDaemon = true } }
    private val cleanupExecutor = Executors.newSingleThreadExecutor { work -> Thread(work, "CarVoiceCleanup").apply { isDaemon = true } }
    private val generation = AtomicLong()
    private val outstandingCleanup = AtomicInteger()
    private val models = SpeechModelStore(carContext)
    private val audioManager = carContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var active: Capture? = null // Accessed only on the main thread.
    private var retiring: Capture? = null // Blocks restart until worker AND host cleanup have finished.
    private var closed = false

    private class Capture(val generation: Long) {
        val cancelled = AtomicBoolean()
        val timedOut = AtomicBoolean()
        val workerFinished = AtomicBoolean()
        val pendingCleanup = AtomicInteger()
        private var workerStarted = false
        var focus: AudioFocusRequest? = null
        var future: Future<*>? = null
        var timeout: Runnable? = null
        private var recorder: CarAudioRecord? = null
        private var stream: InputStream? = null
        private val hostLock = Any()

        @Synchronized
        fun beginWorker(): Boolean {
            // A future cancelled while queued never runs its finally block, so retirement marks it finished.
            if (workerFinished.get()) return false
            workerStarted = true
            return true
        }

        @Synchronized
        fun retire() {
            cancelled.set(true)
            if (!workerStarted) workerFinished.set(true)
        }

        fun begin(record: CarAudioRecord): Boolean {
            synchronized(this) {
                if (cancelled.get() || timedOut.get()) return false
                recorder = record
            }
            // Only worker/cleanup threads acquire this lock. A stalled host call cannot block the UI.
            synchronized(hostLock) {
                if (cancelled.get() || timedOut.get()) return false
                record.startRecording()
            }
            return !cancelled.get() && !timedOut.get()
        }

        @Synchronized
        fun attachStream(input: InputStream): Boolean {
            if (cancelled.get() || timedOut.get()) return false
            stream = input
            return true
        }

        fun stopCapture() {
            val resources = synchronized(this) {
                val result = recorder to stream
                recorder = null
                stream = null
                result
            }
            try { resources.second?.close() } catch (_: Exception) { }
            stopUnattached(resources.first, null)
        }

        fun stopUnattached(record: CarAudioRecord?, input: InputStream?) {
            try { input?.close() } catch (_: Exception) { }
            synchronized(hostLock) {
                try { record?.stopRecording() } catch (_: Exception) { }
            }
        }
    }

    fun start() = onMain {
        if (closed || active != null) return@onMain
        if (retiring != null || outstandingCleanup.get() != 0) {
            onError("De vorige opname wordt nog afgesloten. Wacht tot de automicrofoon is vrijgegeven en probeer opnieuw.")
            return@onMain
        }
        if (carContext.carAppApiLevel < 5) {
            onError("Spraakopname vereist Android Auto met Car App API 5 of hoger. Werk Android Auto bij.")
            return@onMain
        }
        if (carContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            onError("Geef eerst microfoontoestemming in de configuratie terwijl je geparkeerd bent.")
            return@onMain
        }
        if (!models.isReady()) {
            onError("Installeer eerst het Nederlandse offline spraakmodel via de telefoonconfiguratie.")
            return@onMain
        }
        val capture = Capture(generation.incrementAndGet())
        active = capture
        val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setOnAudioFocusChangeListener({ change ->
                if (change < 0 && active === capture) cancel("De opname is onderbroken door een oproep of andere audio.")
            }, main)
            .build()
        capture.focus = focus
        try {
            if (audioManager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                cancel("De microfoon kan nu geen audiofocus krijgen. Probeer opnieuw.")
                return@onMain
            }
        } catch (_: Exception) {
            cancel("Audiofocus is niet beschikbaar. Probeer opnieuw.")
            return@onMain
        }
        capture.timeout = Runnable {
            if (active === capture) {
                capture.timedOut.set(true)
                // Retire the session and release focus now, even when native/model/host work is blocked.
                cancel("Na 30 seconden is een stopverzoek verzonden. De automicrofoon wordt nog afgesloten.")
            }
        }.also { main.postDelayed(it, MAX_CAPTURE_MS) }
        capture.future = executor.submit {
            if (capture.beginWorker()) {
                try {
                    recognize(capture)
                } finally {
                    // recognize() has queued its final host cleanup before this flag can release the gate.
                    capture.workerFinished.set(true)
                    onMain { finishRetirement(capture) }
                }
            }
        }
    }

    /** Cancels the current command. Partial text is never dispatched as a navigation/music action. */
    fun stop() = onMain { cancel(null) }

    fun close() = onMain {
        if (closed) return@onMain
        closed = true
        cancel(null)
        executor.shutdownNow()
        cleanupExecutor.shutdown()
    }

    private fun recognize(capture: Capture) {
        var model: Model? = null
        var recognizer: Recognizer? = null
        var unattachedRecord: CarAudioRecord? = null
        var unattachedStream: InputStream? = null
        var recognized: String? = null
        var failure: String? = null
        try {
            if (capture.cancelled.get()) return
            if (capture.timedOut.get()) { failure = "De spraakopname kon niet binnen 30 seconden starten. Probeer opnieuw."; return }
            model = Model(models.directory.absolutePath)
            recognizer = Recognizer(model, 16_000f)
            if (capture.cancelled.get()) return
            if (capture.timedOut.get()) { failure = "De spraakopname kon niet binnen 30 seconden starten. Probeer opnieuw."; return }
            if (carContext.carAppApiLevel < 5) {
                failure = "Spraakopname vereist Android Auto met Car App API 5 of hoger. Werk Android Auto bij."
                return
            }
            // Permission can be revoked between the button press and this worker's host call.
            if (carContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                failure = "Microfoontoestemming ontbreekt. Geef toestemming terwijl je geparkeerd bent."
                return
            }
            if (outstandingCleanup.get() != 0) {
                failure = "De vorige automicrofoonopname wordt nog afgesloten. Probeer daarna opnieuw."
                return
            }
            val record = CarAudioRecord.create(carContext)
            unattachedRecord = record
            if (!capture.begin(record)) return
            val stream = CarPcmInputStream { bytes, offset, length -> record.read(bytes, offset, length) }
            unattachedStream = stream
            if (!capture.attachStream(stream)) return
            unattachedRecord = null
            unattachedStream = null
            emit(capture) { if (!capture.timedOut.get()) onListening(true) }
            val buffer = ByteArray(4096)
            while (!capture.cancelled.get() && !capture.timedOut.get() && !Thread.currentThread().isInterrupted) {
                val count = try { stream.read(buffer) } catch (error: Exception) {
                    if (capture.cancelled.get() || capture.timedOut.get()) break else throw error
                }
                if (count < 0) break
                if (count == 0) continue
                if (recognizer.acceptWaveForm(buffer, count)) {
                    val text = recognizedText(recognizer.result)
                    if (text.isNotEmpty()) {
                        recognized = text
                        break
                    }
                }
            }
            if (!capture.cancelled.get() && recognized == null) {
                recognized = recognizedText(recognizer.finalResult).takeIf { it.isNotEmpty() }
                if (recognized == null) failure = if (capture.timedOut.get()) {
                    "Na 30 seconden is geen verstaanbare opdracht herkend. Druk opnieuw op de microfoon."
                } else "Geen verstaanbare opdracht gehoord. Probeer opnieuw."
            }
        } catch (error: Throwable) {
            if (error is VirtualMachineError || error is ThreadDeath) throw error
            if (!capture.cancelled.get()) failure = when (error) {
                is SecurityException -> "Microfoontoegang is geweigerd of ingetrokken. Controleer de toestemming terwijl je geparkeerd bent."
                is LinkageError -> "De offline spraakbibliotheek werkt niet op dit apparaat."
                else -> "De automicrofoon of offline spraakherkenning is niet beschikbaar. Controleer de verbinding en probeer opnieuw."
            }
        } finally {
            // The decoder thread exclusively owns these native objects; cancellation never closes them concurrently.
            try { recognizer?.close() } catch (_: Exception) { }
            try { model?.close() } catch (_: Exception) { }
            val finalText = recognized
            val finalError = failure
            scheduleCleanup(capture) {
                capture.stopCapture()
                capture.stopUnattached(unattachedRecord, unattachedStream)
                emit(capture) {
                    capture.timeout?.let(main::removeCallbacks)
                    capture.focus?.let { try { audioManager.abandonAudioFocusRequest(it) } catch (_: Exception) { } }
                    if (active === capture) active = null
                    onListening(false)
                    if (finalText != null) onResult(finalText) else if (finalError != null) onError(finalError)
                }
            }
        }
    }

    private fun cancel(error: String?) {
        val capture = active
        active = null
        generation.incrementAndGet() // Suppress all queued callbacks from a superseded session.
        if (capture != null) {
            retiring = capture
            capture.retire()
            capture.timeout?.let(main::removeCallbacks)
            capture.future?.cancel(true)
            capture.focus?.let { try { audioManager.abandonAudioFocusRequest(it) } catch (_: Exception) { } }
        }
        onListening(false)
        if (error != null) onError(error)
        if (capture != null) scheduleCleanup(capture) { capture.stopCapture() }
    }

    private fun scheduleCleanup(capture: Capture, cleanup: () -> Unit) {
        // Increment synchronously before queuing; an idle-looking UI must not reopen the host prematurely.
        outstandingCleanup.incrementAndGet()
        capture.pendingCleanup.incrementAndGet()
        val trackedCleanup = {
            try {
                cleanup()
            } finally {
                capture.pendingCleanup.decrementAndGet()
                outstandingCleanup.decrementAndGet()
                onMain { finishRetirement(capture) }
            }
        }
        try {
            cleanupExecutor.execute(trackedCleanup)
        } catch (_: RejectedExecutionException) {
            // A cancelled worker can finish after close() shut down the pool. Never run host cleanup on main.
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Thread(trackedCleanup, "CarVoiceLateCleanup").apply { isDaemon = true }.start()
            } else trackedCleanup()
        }
    }

    private fun finishRetirement(capture: Capture) {
        if (retiring === capture && capture.workerFinished.get() && capture.pendingCleanup.get() == 0) {
            retiring = null
        }
    }

    private fun emit(capture: Capture, callback: () -> Unit) {
        main.post { if (!closed && !capture.cancelled.get() && generation.get() == capture.generation) callback() }
    }

    private fun onMain(callback: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) callback() else main.post(callback)
    }

    companion object {
        private const val MAX_CAPTURE_MS = 30_000L
        private fun recognizedText(result: String): String = JSONObject(result).optString("text", "").trim().take(4000)
    }
}
