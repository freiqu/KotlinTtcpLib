import java.io.InputStream
import java.util.*

class TypeReader(private val inputStream: InputStream) {
    fun readUInt(): UInt {
        val bytes = inputStream.readNBytes(4)
        return ((bytes[0].toUInt() and 0xFFu) shl 24) or
                ((bytes[1].toUInt() and 0xFFu) shl 16) or
                ((bytes[2].toUInt() and 0xFFu) shl 8)  or
                (bytes[3].toUInt() and 0xFFu)
    }
    fun readLong(): Long {
        val bytes = inputStream.readNBytes(8)
        return ((bytes[0].toLong() and 0xFF) shl 56) or
                ((bytes[1].toLong() and 0xFF) shl 48) or
                ((bytes[2].toLong() and 0xFF) shl 40) or
                ((bytes[3].toLong() and 0xFF) shl 32) or
                ((bytes[4].toLong() and 0xFF) shl 24) or
                ((bytes[5].toLong() and 0xFF) shl 16) or
                ((bytes[6].toLong() and 0xFF) shl 8) or
                (bytes[7].toLong() and 0xFF)
    }
    fun readBoolean(): Boolean {
        val byte = inputStream.readNBytes(1)[0]
        return 0x00.toByte() != byte
    }
    fun readString(): String {
        val length = readUInt()
        val bytes = inputStream.readNBytes(length.toInt())
        return bytes.toString(Charsets.UTF_8)
    }
    fun readDate(): Date {
        return Date(readLong())
    }
    fun readByte(): Byte {
        return inputStream.readNBytes(1)[0]
    }
    private inline fun <reified T> readArray(readFunction: () -> T): Array<T> {
        val length = readUInt()
        return Array(length.toInt()) { _: Int -> readFunction() }
    }
    fun readIntArray(): Array<UInt> {
        return readArray { readUInt() }
    }
    fun readStringArray(): Array<String> {
        return readArray { readString() }
    }
    fun readByteArray(): ByteArray {
        val length = readUInt()
        return inputStream.readNBytes(length.toInt())
    }
    fun bufferAhead(size: Int) {}
    fun availableBytes(): Int {
        return inputStream.available()
    }
}