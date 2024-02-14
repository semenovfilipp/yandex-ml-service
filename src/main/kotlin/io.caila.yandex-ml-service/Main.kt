import com.mlp.sdk.MlpException
import com.mlp.sdk.MlpPredictServiceBase
import com.mlp.sdk.MlpServiceSDK

data class SimpleTestActionRequest( // 1
    val action: String,
    val name: String
)
class SimpleTestAction : MlpPredictServiceBase<SimpleTestActionRequest, String>(REQUEST_EXAMPLE, RESPONSE_EXAMPLE) { // 2

    override fun predict(req: SimpleTestActionRequest): String { // 3
        return when (req.action) {
            "hello" -> "Hello ${req.name}!"
            else -> throw MlpException("actionUnknownException")
        }
    }

    companion object { // 4
        val REQUEST_EXAMPLE = SimpleTestActionRequest("hello", "World")
        val RESPONSE_EXAMPLE = "Hello World!"
    }

}
fun main() {
    val action = SimpleTestAction() // 1
    val actionSDK = MlpServiceSDK(action) // 2

    actionSDK.start() // 3
    actionSDK.blockUntilShutdown() // 4
}
