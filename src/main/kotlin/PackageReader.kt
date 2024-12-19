import kotlin.Error

class PackageReader(private val typeReader: TypeReader) {
    fun readNextPackage(): Package {
        try {
            val version = typeReader.readByte()
            if (version != 0x01u.toByte()) throw Error("Package version $version is not supported")
            val length = typeReader.readUInt()
            typeReader.bufferAhead(length.toInt() - 5)
            val packageID: PackageID = PackageID.fromByte(typeReader.readByte().toUByte())
            val timestamp = typeReader.readDate()
            return when (packageID) {
                PackageID.ERROR -> ErrorPackage.readFromTypeReader(typeReader, timestamp)
                PackageID.CONNECTION_REQUEST -> ConnectionRequest.readFromTypeReader(typeReader, timestamp)
                PackageID.ID_RECLAIM -> IdReclaim.readFromTypeReader(typeReader, timestamp)
                PackageID.CONNECTION_RESPONSE -> ConnectionRequestResponse.readFromTypeReader(typeReader, timestamp)
                PackageID.SERVER_CONNECTION_RESPONSE -> ServerConnectionResponse.readFromTypeReader(typeReader, timestamp)
                PackageID.CONNECTION_TERMINATION -> ConnectionTermination.readFromTypeReader(typeReader, timestamp)
                PackageID.CLIENT_UPDATE -> ClientUpdate.readFromTypeReader(typeReader, timestamp)
                PackageID.CLIENT_LOGOUT -> ClientLogout.readFromTypeReader(typeReader, timestamp)
                PackageID.RENAME_REQUEST -> RenameRequest.readFromTypeReader(typeReader, timestamp)
                PackageID.TEXT_MESSAGE -> TextMessage.readFromTypeReader(typeReader, timestamp)
                PackageID.FILE_MESSAGE -> FileMessage.readFromTypeReader(typeReader, timestamp)
                PackageID.TEXT_DIRECT_MESSAGE -> TextDM.readFromTypeReader(typeReader, timestamp)
                PackageID.FILE_DIRECT_MESSAGE -> FileDM.readFromTypeReader(typeReader, timestamp)
            }
        } catch (e: IllegalArgumentException) {
            throw InvalidPackageIdException()
        }
    }
    fun available(): Boolean {
        return typeReader.availableBytes() > 0
    }
}