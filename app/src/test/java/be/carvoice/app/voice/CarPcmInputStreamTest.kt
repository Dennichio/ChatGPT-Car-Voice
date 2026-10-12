package be.carvoice.app.voice

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class CarPcmInputStreamTest {
    @Test fun preservesOddReadsAndOffsetsWithoutLosingSamples() {
        val source = byteArrayOf(1, 2, 3, 4, 5, 6)
        var position = 0
        val stream = CarPcmInputStream { target, offset, length ->
            assertEquals(0, length % 2)
            val count = minOf(length, source.size - position)
            if (count == 0) -1 else {
                source.copyInto(target, offset, position, position + count)
                position += count
                count
            }
        }
        assertEquals(1, stream.read())
        val buffer = ByteArray(8)
        assertEquals(5, stream.read(buffer, 2, 5))
        assertArrayEquals(byteArrayOf(0, 0, 2, 3, 4, 5, 6, 0), buffer)
        assertEquals(-1, stream.read())
    }

    @Test fun closePreventsFurtherMicrophoneReads() {
        var reads = 0
        val stream = CarPcmInputStream { _, _, _ -> reads++; 0 }
        stream.close()
        assertEquals(-1, stream.read(ByteArray(8)))
        assertEquals(-1, stream.read())
        assertEquals(0, reads)
    }

    @Test(expected = java.io.IOException::class)
    fun rejectsImpossibleNativeReadCount() {
        CarPcmInputStream { buffer, _, _ -> buffer.size + 1 }.read(ByteArray(8))
    }

    @Test fun reassemblesOddRawFragmentsAndDropsTruncatedEofSample() {
        val fragments = ArrayDeque(listOf(byteArrayOf(1), byteArrayOf(2, 3, 4), byteArrayOf(5, 6, 7, 8, 9)))
        val stream = CarPcmInputStream { buffer, offset, _ ->
            if (fragments.isEmpty()) -1 else fragments.removeFirst().also { it.copyInto(buffer, offset) }.size
        }
        val result = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8)
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            assertEquals(0, count % 2)
            result.write(buffer, 0, count)
        }
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8), result.toByteArray())
    }

    @Test fun loneTruncatedByteDoesNotReachDecoder() {
        var first = true
        val stream = CarPcmInputStream { buffer, offset, _ ->
            if (first) { first = false; buffer[offset] = 42; 1 } else -1
        }
        assertEquals(-1, stream.read(ByteArray(8)))
        assertEquals(-1, stream.read())
    }
}
