package be.carvoice.app.voice

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ModelArchiveTest {
    private fun withArchive(entries: Map<String, ByteArray>, test: (File, File) -> Unit) {
        val root = Files.createTempDirectory("model-archive-test").toFile()
        try {
            val archive = File(root, "model.zip")
            ZipOutputStream(archive.outputStream()).use { zip ->
                entries.forEach { (name, bytes) -> zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry() }
            }
            val staging = File(root, "staging").apply { mkdir() }
            test(archive, staging)
        } finally { root.deleteRecursively() }
    }

    private fun files() = linkedMapOf(
        "${ModelArchive.MODEL_NAME}/am/final.mdl" to byteArrayOf(1),
        "${ModelArchive.MODEL_NAME}/conf/mfcc.conf" to byteArrayOf(2),
        "${ModelArchive.MODEL_NAME}/conf/model.conf" to byteArrayOf(3),
        "${ModelArchive.MODEL_NAME}/graph/HCLr.fst" to byteArrayOf(4),
        "${ModelArchive.MODEL_NAME}/graph/Gr.fst" to byteArrayOf(5),
    )

    @Test fun extractsExpectedStructure() = withArchive(files()) { archive, staging ->
        ModelArchive.extract(archive, staging)
        assertTrue(ModelArchive.hasStructure(File(staging, ModelArchive.MODEL_NAME)))
    }

    @Test fun acceptsEmptyDirectoryEntriesInRealModelLayout() = withArchive(linkedMapOf(
        "${ModelArchive.MODEL_NAME}/" to byteArrayOf(),
        "${ModelArchive.MODEL_NAME}/conf/" to byteArrayOf(),
    ).apply { putAll(files()) }) { archive, staging ->
        ModelArchive.extract(archive, staging)
        assertTrue(ModelArchive.hasStructure(File(staging, ModelArchive.MODEL_NAME)))
    }

    @Test(expected = java.io.IOException::class)
    fun rejectsParentTraversal() = withArchive(mapOf("${ModelArchive.MODEL_NAME}/../../escape" to byteArrayOf(1))) { archive, staging ->
        ModelArchive.extract(archive, staging)
    }

    @Test(expected = java.io.IOException::class)
    fun rejectsAbsolutePath() = withArchive(mapOf("/escape" to byteArrayOf(1))) { archive, staging -> ModelArchive.extract(archive, staging) }

    @Test(expected = java.io.IOException::class)
    fun rejectsForeignModelRoot() = withArchive(mapOf("other-model/am/final.mdl" to byteArrayOf(1))) { archive, staging -> ModelArchive.extract(archive, staging) }

    @Test(expected = java.io.IOException::class)
    fun rejectsMissingModelFiles() = withArchive(mapOf("${ModelArchive.MODEL_NAME}/am/final.mdl" to byteArrayOf(1))) { archive, staging -> ModelArchive.extract(archive, staging) }

    @Test(expected = java.io.IOException::class)
    fun boundsExpandedSize() = withArchive(files()) { archive, staging ->
        ModelArchive.extract(archive, staging, ModelArchive.Limits(expandedBytes = 3))
    }

    @Test(expected = java.io.IOException::class)
    fun boundsEntryCount() = withArchive(files()) { archive, staging -> ModelArchive.extract(archive, staging, ModelArchive.Limits(entries = 2)) }

    @Test(expected = java.io.IOException::class)
    fun boundsSingleFileSize() = withArchive(mapOf("${ModelArchive.MODEL_NAME}/am/final.mdl" to ByteArray(20))) { archive, staging ->
        ModelArchive.extract(archive, staging, ModelArchive.Limits(fileBytes = 10))
    }

    @Test(expected = java.io.IOException::class)
    fun rejectsWindowsTraversal() = withArchive(mapOf("${ModelArchive.MODEL_NAME}\\..\\escape" to byteArrayOf(1))) { archive, staging ->
        ModelArchive.extract(archive, staging)
    }

    @Test(expected = java.io.IOException::class)
    fun rejectsDotSegmentAliases() = withArchive(mapOf("${ModelArchive.MODEL_NAME}/conf/./model.conf" to byteArrayOf(1))) { archive, staging ->
        ModelArchive.extract(archive, staging)
    }

    @Test(expected = java.io.IOException::class)
    fun rejectsFileDirectoryCollision() = withArchive(linkedMapOf(
        "${ModelArchive.MODEL_NAME}/conf" to byteArrayOf(1),
        "${ModelArchive.MODEL_NAME}/conf/model.conf" to byteArrayOf(2),
    )) { archive, staging -> ModelArchive.extract(archive, staging) }

    @Test(expected = kotlinx.coroutines.CancellationException::class)
    fun propagatesCancellationDuringExtraction() = withArchive(files()) { archive, staging ->
        var checks = 0
        ModelArchive.extract(archive, staging) { if (++checks == 3) throw kotlinx.coroutines.CancellationException("test") }
    }

    @Test(expected = java.io.IOException::class)
    fun rejectsDirectoryEntryWithHiddenCompressedPayload() = withArchive(
        mapOf("${ModelArchive.MODEL_NAME}/conf/" to ByteArray(64 * 1024)),
    ) { archive, staging ->
        ModelArchive.extract(archive, staging, ModelArchive.Limits(expandedBytes = 8))
    }
}
