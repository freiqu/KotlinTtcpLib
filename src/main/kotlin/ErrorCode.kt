enum class ErrorCode(val id: UByte) {
    ALREADY_LOGGED_IN(0x05u),
    NOT_CONNECTED_YET(0x06u),
    RECIPIENT_NOT_FOUND(0x06u),
    INVALID_PACKAGE_ID(0x02u),
    ID_NOT_FOUND(0x01u);

    companion object {
        fun fromByte(byte: UByte): ErrorCode {
            return entries.find { it.id == byte } ?: throw IllegalArgumentException("Unknown PackageID: $byte")
        }
    }
}