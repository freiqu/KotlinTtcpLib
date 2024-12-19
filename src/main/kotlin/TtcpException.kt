import java.lang.RuntimeException

open class TtcpException(val code: ErrorCode, val description: String = ""): RuntimeException()
{
}