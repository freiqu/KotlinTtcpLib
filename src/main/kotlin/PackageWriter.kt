import java.io.OutputStream

class PackageWriter(private val outputStream: OutputStream) {
    fun writePackage(pkg: Package) {
        outputStream.write(pkg.toBytes())
        outputStream.flush()
    }
}