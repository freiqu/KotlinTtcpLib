import java.util.Date

enum class PackageID(val id: UByte) {
    ERROR(0x00u),
    CONNECTION_REQUEST(0x01u),
    ID_RECLAIM(0x02u),
    CONNECTION_RESPONSE(0x03u),
    SERVER_CONNECTION_RESPONSE(0x04u),
    CONNECTION_TERMINATION(0x05u),
    CLIENT_UPDATE(0x06u),
    CLIENT_LOGOUT(0x07u),
    RENAME_REQUEST(0x08u),
    TEXT_MESSAGE(0x10u),
    FILE_MESSAGE(0x11u),
    TEXT_DIRECT_MESSAGE(0x12u),
    FILE_DIRECT_MESSAGE(0x13u);

    companion object {
        fun fromByte(byte: UByte): PackageID {
            return entries.find { it.id == byte } ?: throw IllegalArgumentException("Unknown PackageID: $byte")
        }
    }
}

enum class TerminationType(val id: UByte) {
    LOGOUT(0x00u),
    DELETE(0x01u);
    companion object {
        fun fromByte(byte: UByte): TerminationType {
            return entries.find { it.id == byte } ?: throw IllegalArgumentException("Unknown Termination Type: $byte")
        }
    }
}

abstract class Package(val packetId: PackageID, val timestamp: Date) {
    private val ttcpVersion: Byte = 0x01

    open fun toBytes(): ByteArray {
        return byteArrayOf(ttcpVersion, *getLength().toBytes(), packetId.id.toByte(), *timestamp.time.toBytes())
    }

    open fun getLength(): UInt {
        return 14u
    }
}

class ErrorPackage(val code: ErrorCode, val description: String, timestamp: Date = Date()) :
    Package(PackageID.ERROR, timestamp) {
    private val descriptionBytes = description.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), code.id.toByte(), *descriptionBytes)
    }

    override fun getLength(): UInt {
        return super.getLength() + 1u + descriptionBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): ErrorPackage {
            val code = ErrorCode.fromByte(typeReader.readByte().toUByte())
            val details = typeReader.readString()
            return ErrorPackage(code, details, timestamp)
        }
    }
}

class ConnectionRequest(val name: String, timestamp: Date = Date()) :
    Package(PackageID.CONNECTION_REQUEST, timestamp) {
    private val nameBytes = name.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), *nameBytes)
    }

    override fun getLength(): UInt {
        return super.getLength() + nameBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): ConnectionRequest {
            val name = typeReader.readString()
            return ConnectionRequest(name, timestamp)
        }
    }
}

class IdReclaim(val id: UInt, timestamp: Date = Date()) :
    Package(PackageID.ID_RECLAIM, timestamp) {
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), *id.toBytes())
    }

    override fun getLength(): UInt {
        return super.getLength() + 4u
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): IdReclaim {
            val id = typeReader.readUInt()
            return IdReclaim(id, timestamp)
        }
    }
}

class ConnectionRequestResponse(val name: String?, val accepted: Boolean, timestamp: Date = Date()) :
    Package(PackageID.CONNECTION_RESPONSE, timestamp) {
    private val nameBytes = name?.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return if (nameBytes != null) {
            byteArrayOf(*super.toBytes(), accepted.toByte(), *nameBytes)
        } else {
            byteArrayOf(*super.toBytes(), accepted.toByte(), *0u.toBytes())
        }
    }

    override fun getLength(): UInt {
        return if (nameBytes != null) {
            super.getLength() + 1u + nameBytes.size.toUInt()
        } else {
            super.getLength() + 1u + 4u
        }
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): ConnectionRequestResponse {
            val accepted = typeReader.readBoolean()
            val name = typeReader.readString()
            return ConnectionRequestResponse(name, accepted, timestamp)
        }
    }
}

class ServerConnectionResponse(val id: UInt, val users: Map<UInt, String>, timestamp: Date = Date()) :
    Package(PackageID.SERVER_CONNECTION_RESPONSE, timestamp) {
    private val idListBytes = users.keys.stream().flatMap { it.toBytes().toList().stream() }.toList().toByteArray()
    private val nameBytes = users.values.stream().flatMap { it.toTtcpBytes().toList().stream() }.toList().toByteArray()

    override fun toBytes(): ByteArray {
        val lengthBytes = users.size.toUInt().toBytes()
        return byteArrayOf(*super.toBytes(), *id.toBytes(), *lengthBytes, *idListBytes, *lengthBytes, *nameBytes)
    }

    override fun getLength(): UInt {
        return super.getLength() + 4u + 4u + idListBytes.size.toUInt() + 4u + nameBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): ServerConnectionResponse {
            val id = typeReader.readUInt()
            val idList = typeReader.readIntArray()
            val nameList = typeReader.readStringArray()
            val users = idList.zip(nameList).toMap()
            return ServerConnectionResponse(id, users, timestamp)
        }
    }
}

class ConnectionTermination(val type: TerminationType, timestamp: Date = Date()) :
    Package(PackageID.CONNECTION_TERMINATION, timestamp) {
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), type.id.toByte())
    }

    override fun getLength(): UInt {
        return super.getLength() + 1u
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): ConnectionTermination {
            val type = TerminationType.fromByte(typeReader.readByte().toUByte())
            return ConnectionTermination(type, timestamp)
        }
    }
}

class ClientUpdate(val id: UInt, val name: String, timestamp: Date = Date()) :
    Package(PackageID.CLIENT_UPDATE, timestamp) {
    private val nameBytes = name.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), *id.toBytes(), *nameBytes)
    }

    override fun getLength(): UInt {
        return super.getLength() + 4u + nameBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): ClientUpdate {
            val id = typeReader.readUInt()
            val name = typeReader.readString()
            return ClientUpdate(id, name, timestamp)
        }
    }
}

class ClientLogout(val id: UInt, timestamp: Date = Date()) :
    Package(PackageID.CLIENT_LOGOUT, timestamp) {
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), *id.toBytes())
    }

    override fun getLength(): UInt {
        return super.getLength() + 4u
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): ClientLogout {
            val id = typeReader.readUInt()
            return ClientLogout(id, timestamp)
        }
    }
}

class RenameRequest(val name: String, timestamp: Date = Date()) :
    Package(PackageID.RENAME_REQUEST, timestamp) {
    private val nameBytes = name.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), *nameBytes)
    }

    override fun getLength(): UInt {
        return super.getLength() + nameBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): RenameRequest {
            val name = typeReader.readString()
            return RenameRequest(name, timestamp)
        }
    }
}

class TextMessage(val msg: String, timestamp: Date = Date()) :
    Package(PackageID.TEXT_MESSAGE, timestamp) {
    private val msgBytes = msg.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), *msgBytes)
    }

    override fun getLength(): UInt {
        return super.getLength() + msgBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): TextMessage {
            val msg = typeReader.readString()
            return TextMessage(msg, timestamp)
        }
    }
}

class FileMessage(
    val mimeType: String,
    val fileName: String,
    val description: String,
    val fileBytes: ByteArray,
    timestamp: Date = Date()
) :
    Package(PackageID.FILE_MESSAGE, timestamp) {
    private val mimeTypeBytes = mimeType.toTtcpBytes()
    private val fileNameBytes = fileName.toTtcpBytes()
    private val descriptionBytes = description.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(
            *super.toBytes(),
            *mimeTypeBytes,
            *fileNameBytes,
            *descriptionBytes,
            *fileBytes.size.toUInt().toBytes(),
            *fileBytes
        )
    }

    override fun getLength(): UInt {
        return super.getLength() + mimeTypeBytes.size.toUInt() + fileNameBytes.size.toUInt() + descriptionBytes.size.toUInt() + 4u + fileBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): FileMessage {
            val mimeType = typeReader.readString()
            val fileName = typeReader.readString()
            val description = typeReader.readString()
            val fileBytes = typeReader.readByteArray()
            return FileMessage(mimeType, fileName, description, fileBytes, timestamp)
        }
    }
}

class TextDM(override val recipientId: UInt, override val senderId: UInt, val msg: String, timestamp: Date = Date()) :
    Package(PackageID.TEXT_DIRECT_MESSAGE, timestamp), DirectMessage {
    private val msgBytes = msg.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(*super.toBytes(), *recipientId.toBytes(), *senderId.toBytes(), *msgBytes)
    }

    override fun getLength(): UInt {
        return super.getLength() + 4u + 4u + msgBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): TextDM {
            val recipientId = typeReader.readUInt()
            val senderId = typeReader.readUInt()
            val msg = typeReader.readString()
            return TextDM(recipientId, senderId, msg, timestamp)
        }
    }
}

class FileDM(
    override val recipientId: UInt,
    override val senderId: UInt,
    val mimeType: String,
    val fileName: String,
    val description: String,
    val fileBytes: ByteArray,
    timestamp: Date = Date()
) :
    Package(PackageID.FILE_DIRECT_MESSAGE, timestamp), DirectMessage {
    private val mimeTypeBytes = mimeType.toTtcpBytes()
    private val fileNameBytes = fileName.toTtcpBytes()
    private val descriptionBytes = description.toTtcpBytes()
    override fun toBytes(): ByteArray {
        return byteArrayOf(
            *super.toBytes(),
            *recipientId.toBytes(),
            *senderId.toBytes(),
            *mimeTypeBytes,
            *fileNameBytes,
            *descriptionBytes,
            *fileBytes.size.toUInt().toBytes(),
            *fileBytes
        )
    }

    override fun getLength(): UInt {
        return super.getLength() + 4u + 4u + mimeTypeBytes.size.toUInt() + fileNameBytes.size.toUInt() + descriptionBytes.size.toUInt() + 4u + fileBytes.size.toUInt()
    }
    companion object {
        fun readFromTypeReader(typeReader: TypeReader, timestamp: Date): FileDM {
            val recipientId = typeReader.readUInt()
            val senderId = typeReader.readUInt()
            val mimeType = typeReader.readString()
            val fileName = typeReader.readString()
            val description = typeReader.readString()
            val fileBytes = typeReader.readByteArray()
            return FileDM(recipientId, senderId, mimeType, fileName, description, fileBytes, timestamp)
        }
    }
}

interface DirectMessage {
    val recipientId: UInt
    val senderId: UInt
}