package io.caila.yandex_ml_service

import com.mlp.sdk.MlpClientSDK
import com.mlp.sdk.MlpService
import com.mlp.sdk.MlpServiceSDK
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionRequest
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionResult

class Main :MlpSer {
    override fun predict(req: ChatCompletionRequest): ChatCompletionResult {
        val message = sendMessageToYandex(req)
        val result = ChatCompletionResult(
            null,
            message,
            created = System.currentTimeMillis(),
            "",
            emptyList(),
            null
        )
        return result
    }
}
fun main() {
    val service = MlpServiceSDK
    actionSDK.start()
    actionSDK.blockUntilShutdown()
}

