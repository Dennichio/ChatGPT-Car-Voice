package be.carvoice.app.voice

import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

/** Adapts CarAudioRecord.read to a stream while preserving complete 16-bit PCM frames. */
internal class CarPcmInputStream(private val readFrames: (ByteArray, Int, Int) -> Int) : InputStream() {
    private val closed = AtomicBoolean()
    private val fragment = ByteArray(4096)
    private val completeFrames = ByteArray(4098)
    private var position = 0
    private var available = 0
    private var carry = -1
    private var eof = false

    override fun read(): Int {
        if (closed.get()) return -1
        if (position == available && !fill()) return -1
        return completeFrames[position++].toInt() and 255
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (offset < 0 || length < 0 || length > buffer.size - offset) throw IndexOutOfBoundsException()
        if (length == 0) return 0
        if (closed.get()) return -1
        if (position == available && !fill()) return -1
        val count = minOf(length, available - position)
        completeFrames.copyInto(buffer, offset, position, position + count)
        position += count
        return count
    }

    private fun fill(): Boolean {
        while (!closed.get() && !eof) {
            val count = readFrames(fragment, 0, fragment.size)
            if (count < 0) {
                eof = true
                carry = -1 // A trailing incomplete 16-bit sample must never reach Vosk.
                return false
            }
            if (count == 0) continue
            if (count > fragment.size) throw IOException("Invalid PCM fragment size.")
            val prefix = if (carry >= 0) 1 else 0
            if (prefix == 1) completeFrames[0] = carry.toByte()
            fragment.copyInto(completeFrames, prefix, 0, count)
            val total = prefix + count
            available = total and -2
            carry = if (total % 2 != 0) completeFrames[total - 1].toInt() and 255 else -1
            position = 0
            if (available > 0) return true
        }
        return false
    }

    override fun close() { closed.set(true) }
}
