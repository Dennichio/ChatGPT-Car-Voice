package be.carvoice.app.voice

import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream

/** Extracts one known model into a fresh private staging directory. No arbitrary archive imports. */
internal object ModelArchive {
    const val MODEL_NAME = "vosk-model-small-nl-0.22"
    data class Limits(val entries: Int = 10_000, val expandedBytes: Long = 384L * 1024 * 1024, val fileBytes: Long = 192L * 1024 * 1024)

    fun extract(archive: File, staging: File, limits: Limits = Limits(), checkCancelled: () -> Unit = {}) {
        check(staging.isDirectory && staging.listFiles().orEmpty().isEmpty()) { "Staging must be empty." }
        val root = staging.canonicalFile
        var entries = 0
        var expanded = 0L
        val names = mutableSetOf<String>()
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            while (true) {
                checkCancelled()
                val entry = zip.nextEntry ?: break
                if (++entries > limits.entries) throw IOException("Archive contains too many entries.")
                val name = entry.name
                val normalized = name.removeSuffix("/")
                if (name.contains('\\') || name.contains('\u0000') || name.startsWith('/') || name.contains(':')
                    || normalized.split('/').any { it.isEmpty() || it == "." || it == ".." }
                    || (normalized != MODEL_NAME && !normalized.startsWith("$MODEL_NAME/"))
                    || !names.add(normalized)
                ) throw IOException("Unsafe model archive path.")
                val target = File(root, normalized).canonicalFile
                if (!target.path.startsWith(root.path + File.separator)) throw IOException("Archive path escapes staging.")
                if (entry.isDirectory) {
                    // ZIP permits payloads on directory entries. Never let closeEntry silently inflate a bomb.
                    checkCancelled()
                    if (entry.size > 0 || zip.read() != -1) throw IOException("Directory entry contains a payload.")
                    checkCancelled()
                    if (!target.mkdirs() && !target.isDirectory) throw IOException("Cannot create model directory.")
                } else {
                    if (entry.size > limits.fileBytes) throw IOException("Model file exceeds limit.")
                    if (!target.parentFile!!.mkdirs() && !target.parentFile!!.isDirectory) throw IOException("Cannot create model directory.")
                    if (target.exists()) throw IOException("Duplicate model file.")
                    var fileBytes = 0L
                    target.outputStream().buffered().use { output ->
                        val buffer = ByteArray(32 * 1024)
                        while (true) {
                            checkCancelled()
                            val count = zip.read(buffer)
                            if (count < 0) break
                            fileBytes += count
                            expanded += count
                            if (fileBytes > limits.fileBytes || expanded > limits.expandedBytes) throw IOException("Expanded model exceeds limit.")
                            output.write(buffer, 0, count)
                        }
                    }
                }
                zip.closeEntry()
            }
        }
        if (entries == 0) throw IOException("Empty model archive.")
        if (!hasStructure(File(root, MODEL_NAME))) throw IOException("Incomplete Vosk model.")
    }

    fun hasStructure(directory: File): Boolean {
        if (!directory.isDirectory) return false
        val base = directory.canonicalFile
        fun valid(path: String): Boolean {
            val file = File(directory, path)
            return file.isFile && file.length() > 0 && file.canonicalPath.startsWith(base.path + File.separator)
        }
        return valid("am/final.mdl") && valid("conf/mfcc.conf") && valid("conf/model.conf")
            && (valid("graph/HCLG.fst") || (valid("graph/HCLr.fst") && valid("graph/Gr.fst")))
    }
}
