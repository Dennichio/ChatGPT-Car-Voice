package be.carvoice.app.car

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import java.util.Locale

/** Short destination confirmations, with Android-selected output routing and bounded focus. */
internal class CarSpeechFeedback(private val context: CarContext) {
    private val handler = Handler(Looper.getMainLooper())
    private val audio = context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private var engine: TextToSpeech? = null
    private var ready = false
    private var closed = false
    private var generation = 0L
    private var focus: AudioFocusRequest? = null
    private var completion: (() -> Unit)? = null
    private var timeout: Runnable? = null

    init {
        engine = TextToSpeech(context.applicationContext) { result -> handler.post {
            if (!closed) {
                ready = result == TextToSpeech.SUCCESS
                if (ready) {
                    engine?.setAudioAttributes(attributes)
                    var language = engine?.setLanguage(Locale.forLanguageTag("nl-BE")) ?: TextToSpeech.LANG_NOT_SUPPORTED
                    if (language < 0) language = engine?.setLanguage(Locale.forLanguageTag("nl-NL")) ?: TextToSpeech.LANG_NOT_SUPPORTED
                    ready = language >= 0
                }
            }
        } }
        engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) { handler.post { finish(utteranceId, false) } }
            @Deprecated("Android compatibility callback")
            override fun onError(utteranceId: String?) { handler.post { finish(utteranceId, true) } }
            override fun onError(utteranceId: String?, errorCode: Int) { handler.post { finish(utteranceId, true) } }
        })
    }

    fun speakThen(text: String, action: () -> Unit) {
        if (closed) return
        stop()
        if (!ready) {
            unavailable()
            action()
            return
        }
        val turn = ++generation
        completion = action
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener({ change ->
                if (change < 0 && turn == generation) stop()
            }, handler).build()
        focus = request
        val granted = try { audio.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED }
        catch (_: RuntimeException) { false }
        if (!granted) {
            finish(turn.toString(), true)
            return
        }
        if (turn != generation) return
        if (engine?.speak(text.take(250), TextToSpeech.QUEUE_FLUSH, null, turn.toString()) != TextToSpeech.SUCCESS) {
            finish(turn.toString(), true)
            return
        }
        timeout = Runnable { finish(turn.toString(), true) }.also { handler.postDelayed(it, 7_000) }
    }

    private fun finish(id: String?, failed: Boolean) {
        if (closed || id?.toLongOrNull() != generation) return
        val action = completion
        completion = null
        stop()
        if (failed) unavailable()
        action?.invoke()
    }

    private fun unavailable() {
        CarToast.makeText(context, "Gesproken bevestiging niet beschikbaar", CarToast.LENGTH_LONG).show()
    }

    fun stop() {
        ++generation
        completion = null
        timeout?.let(handler::removeCallbacks)
        timeout = null
        try { engine?.stop() } catch (_: RuntimeException) { }
        focus?.let { try { audio.abandonAudioFocusRequest(it) } catch (_: RuntimeException) { } }
        focus = null
    }

    fun close() {
        if (closed) return
        stop()
        closed = true
        engine?.shutdown()
        engine = null
    }
}
