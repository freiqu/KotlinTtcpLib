import java.nio.ByteBuffer

fun UInt.toBytes(): ByteArray = (3 downTo 0).map {
    (this shr (it * Byte.SIZE_BITS)).toByte()
}.toByteArray()

fun Long.toBytes(): ByteArray {
    return ByteBuffer.allocate(8).putLong(this).array()
}

fun Boolean.toByte(): Byte {
    return if (this) {
        0x01
    } else {
        0x00
    }
}

fun String.toTtcpBytes(): ByteArray {
    val bytes = this.toByteArray(Charsets.UTF_8)
    return byteArrayOf(*bytes.size.toUInt().toBytes(), *bytes)
}

val STANDARD_PORT = 53111