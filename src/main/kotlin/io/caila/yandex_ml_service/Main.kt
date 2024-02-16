package io.caila.yandex_ml_service

import com.mlp.sdk.MlpExecutionContext
import com.mlp.sdk.MlpPredictServiceBase
import com.mlp.sdk.MlpServiceSDK
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionRequest
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionResult

class Main(
    override val context: MlpExecutionContext
) : MlpPredictServiceBase<ChatCompletionRequest, ChatCompletionResult>(REQUEST_EXAMPLE, RESPONSE_EXAMPLE) {

    private val yandexChatService = YandexChatService()

    override fun predict(req: ChatCompletionRequest): ChatCompletionResult {
        val message = yandexChatService.sendMessageToYandex(req)

        return ChatCompletionResult(
            null,
            message,
            created = System.currentTimeMillis(),
            "",
            emptyList(),
            null
        )
    }

    companion object {
        val REQUEST_EXAMPLE = ChatCompletionRequest(
            model = null,
            messages = emptyList(),
            temperature = null,
            topP = null,
            n = null,
            stream = null,
            stop = emptyList(),
            maxTokens = null,
            presencePenalty = null,
            frequencyPenalty = null,
            logitBias = null,
            user = null
        )
        val RESPONSE_EXAMPLE = ChatCompletionResult(
            null,
            "message",
            created = System.currentTimeMillis(),
            "",
            emptyList(),
            null
        )
    }
}

fun main() {
    val actionSDK = MlpServiceSDK({ Main(MlpExecutionContext.systemContext) })

    actionSDK.start()
    actionSDK.blockUntilShutdown()
}

