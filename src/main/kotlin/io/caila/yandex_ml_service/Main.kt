package io.caila.yandex_ml_service

import com.mlp.sdk.MlpPredictServiceBase
import com.mlp.sdk.MlpServiceSDK
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionRequest
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionResult


class Main(req: ChatCompletionRequest, response: ChatCompletionResult) :
    MlpPredictServiceBase<ChatCompletionRequest, ChatCompletionResult>(req, response) {

    private val initConfig = InitConfig()
    private val predictConfig = PredictConfig()
    private val yandexChatService = YandexChatService(initConfig, predictConfig)

    override fun predict(req: ChatCompletionRequest): ChatCompletionResult {
        val message = yandexChatService.sendMessageToYandex(req)
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
    val actionSDK = MlpServiceSDK()
    actionSDK.start()
    actionSDK.blockUntilShutdown()

}
