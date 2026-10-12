package be.carvoice.app.voice

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.vosk.Model
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OfflineModelStatus(
    val ready: Boolean = false,
    val installing: Boolean = false,
    val progressPercent: Int? = null,
    val error: String? = null,
)

/** No download is triggered by construction, a permission grant, or a microphone press. */
class SpeechModelStore(context: Context) {
    private val base = File(context.applicationContext.filesDir, "offline-speech")
    internal val directory = File(base, ModelArchive.MODEL_NAME)
    private val mutex = Mutex()
    private val mutableStatus = MutableStateFlow(OfflineModelStatus(ready = verifiedOnDisk()))
    val status: StateFlow<OfflineModelStatus> = mutableStatus.asStateFlow()

    fun isReady(): Boolean = verifiedOnDisk()

    fun refreshStatus() {
        if (!mutableStatus.value.installing) mutableStatus.value = OfflineModelStatus(ready = verifiedOnDisk())
    }

    suspend fun install(): Result<Unit> = mutex.withLock {
        if (isReady()) {
            mutableStatus.value = OfflineModelStatus(ready = true)
            return@withLock Result.success(Unit)
        }
        mutableStatus.value = OfflineModelStatus(installing = true)
        var archive: File? = null
        var staging: File? = null
        try {
            withTimeout(300_000) { withContext(Dispatchers.IO) {
                if (!base.mkdirs() && !base.isDirectory) throw IOException("Cannot create private model directory.")
                val transaction = UUID.randomUUID().toString()
                archive = File(base, "download-$transaction.zip")
                staging = File(base, "staging-$transaction")
                if (!staging!!.mkdir()) throw IOException("Cannot create staging directory.")
                val resolvedSource = download(archive!!)
                val coroutine = currentCoroutineContext()
                ModelArchive.extract(archive!!, staging!!) { coroutine.ensureActive() }
                val extracted = File(staging!!, ModelArchive.MODEL_NAME)
                coroutine.ensureActive()
                // Native parsing is required before a persisted ready marker can exist.
                Model(extracted.absolutePath).use { /* Validation only; no shared native Model instance. */ }
                coroutine.ensureActive()
                val digest = MessageDigest.getInstance("SHA-256")
                archive!!.inputStream().use { input ->
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        coroutine.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        digest.update(buffer, 0, count)
                    }
                }
                // Records the received bytes; this is NOT a comparison with a trusted upstream checksum.
                File(extracted, ".carvoice-download.json").writeText(JSONObject()
                    .put("source", MODEL_URL).put("resolved_source", resolvedSource)
                    .put("bytes", archive!!.length()).put("sha256", digest.digest().joinToString("") { "%02x".format(it) })
                    .put("native_validated", true).toString(), Charsets.UTF_8)
                File(extracted, READY_MARKER).writeText(READY_VERSION, Charsets.UTF_8)
                if (directory.exists()) {
                    // Existing ready models are never replaced while a voice session can use them.
                    if (verifiedOnDisk()) return@withContext
                    if (!directory.deleteRecursively()) throw IOException("Cannot replace incomplete model.")
                }
                if (!extracted.renameTo(directory)) throw IOException("Cannot atomically publish model.")
                if (!verifiedOnDisk()) throw IOException("Model publication failed verification.")
            } }
            mutableStatus.value = OfflineModelStatus(ready = true, progressPercent = 100)
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            mutableStatus.value = OfflineModelStatus(ready = verifiedOnDisk())
            currentCoroutineContext().ensureActive()
            if (cancelled is TimeoutCancellationException) {
                val message = "Het installeren duurde langer dan vijf minuten. Controleer internet en probeer opnieuw."
                mutableStatus.value = OfflineModelStatus(ready = verifiedOnDisk(), error = message)
                return@withLock Result.failure(IOException(message))
            }
            throw cancelled
        } catch (error: Throwable) {
            if (error is VirtualMachineError || error is ThreadDeath) throw error
            val message = if (error is LinkageError) {
                "De offline spraakbibliotheek werkt niet op dit apparaat."
            } else {
                "Het Nederlandse spraakmodel kon niet veilig worden geïnstalleerd. Controleer internet en vrije opslag en probeer opnieuw."
            }
            mutableStatus.value = OfflineModelStatus(ready = verifiedOnDisk(), error = message)
            Result.failure(IOException(message))
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                archive?.delete()
                staging?.deleteRecursively()
            }
        }
    }

    private suspend fun download(destination: File): String {
        var target = URI(MODEL_URL)
        for (redirect in 0..3) {
            currentCoroutineContext().ensureActive()
            if (target.scheme != "https" || target.host != "alphacephei.com" || target.userInfo != null
                || target.fragment != null || (target.port != -1 && target.port != 443)
            ) throw IOException("Unapproved model download destination.")
            val connection = (target.toURL().openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 10_000
                readTimeout = 20_000
                useCaches = false
                setRequestProperty("Accept", "application/zip, application/octet-stream")
            }
            try {
                // Installed on the IO thread; cancellation closes a blocked socket immediately.
                val result = suspendCancellableCoroutine<URI?> { continuation ->
                    continuation.invokeOnCancellation { connection.disconnect() }
                    try {
                        if (!continuation.isActive) return@suspendCancellableCoroutine
                        val status = connection.responseCode
                        if (status in setOf(301, 302, 303, 307, 308)) {
                            val location = connection.getHeaderField("Location") ?: throw IOException("Invalid model redirect.")
                            if (location.length > 2048) throw IOException("Invalid model redirect.")
                            val next = target.resolve(location)
                            if (continuation.isActive) continuation.resume(next)
                        } else {
                            if (status != 200) throw IOException("Model server is unavailable.")
                            val length = connection.contentLengthLong
                            if (length > MAX_DOWNLOAD_BYTES || length == 0L) throw IOException("Model download exceeds limit.")
                            var downloaded = 0L
                            connection.inputStream.use { input ->
                                destination.outputStream().buffered().use { output ->
                                    val buffer = ByteArray(32 * 1024)
                                    while (true) {
                                        if (!continuation.isActive) throw CancellationException()
                                        val count = input.read(buffer)
                                        if (count < 0) break
                                        downloaded += count
                                        if (downloaded > MAX_DOWNLOAD_BYTES) throw IOException("Model download exceeds limit.")
                                        output.write(buffer, 0, count)
                                        mutableStatus.value = OfflineModelStatus(installing = true,
                                            progressPercent = if (length > 0) ((downloaded * 90 / length).coerceIn(0, 90)).toInt() else null)
                                    }
                                }
                            }
                            if (downloaded == 0L || (length >= 0L && downloaded != length)) throw IOException("Incomplete model download.")
                            if (continuation.isActive) continuation.resume(null)
                        }
                    } catch (error: Throwable) {
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
                }
                if (result == null) return target.toASCIIString()
                target = result
            } finally {
                connection.disconnect()
            }
        }
        throw IOException("Too many model download redirects.")
    }

    private fun verifiedOnDisk(): Boolean = try {
        val marker = File(directory, READY_MARKER)
        marker.isFile && marker.length() <= 128 && marker.readText(Charsets.UTF_8) == READY_VERSION
            && ModelArchive.hasStructure(directory)
    } catch (_: IOException) {
        false
    }

    companion object {
        const val MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-small-nl-0.22.zip"
        private const val MAX_DOWNLOAD_BYTES = 128L * 1024 * 1024
        private const val READY_MARKER = ".carvoice-validated"
        private const val READY_VERSION = "vosk-small-nl-0.22;native-validated-v1"
    }
}

typealias OfflineVoiceModel = SpeechModelStore
