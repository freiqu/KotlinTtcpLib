import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.*
import kotlin.collections.ArrayList

class UtilKtTest {

    @Test
    fun uIntToByteArray() {
        val ui: UInt = 33686018u
        val b = ui.toBytes()
        println(b)
    }
    @Test
    fun LongToByteArray() {
        //val l: Long = -72623859790382856
        val l: Long = -1024
        val b = l.toBytes()
        val bitsets = ArrayList<String>()
        for (i in b) {
            bitsets.add(i.toUByte().toString(2))
        }
        bitsets.joinToString(" ")
        val s = bitsets.joinToString()
        println(s)
    }

    @Test
    fun test() {
        val msg = TextMessage("Hallo, Welt!", Date())
        val inStream= ByteArrayInputStream(msg.toBytes())
        val r = PackageReader(TypeReader(inStream))
        val p = r.readNextPackage()
        println(p.packetId)
    }

}