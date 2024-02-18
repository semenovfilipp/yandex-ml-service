package io.caila.yandex_ml_service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mlp.sdk.MlpExecutionContext
import com.mlp.sdk.MlpPredictServiceBase
import com.mlp.sdk.MlpServiceSDK
import com.mlp.sdk.datatypes.chatgpt.*

class Main(
    override val context: MlpExecutionContext
) : MlpPredictServiceBase<ChatCompletionRequest, ChatCompletionResult>(REQUEST_EXAMPLE, RESPONSE_EXAMPLE) {

    private val yandexChatService = YandexChatService()

    override fun predict(req: ChatCompletionRequest): ChatCompletionResult {
        val resultResponse = yandexChatService.sendMessageToYandex(req)
        val objectMapper = ObjectMapper()

        val choices = resultResponse.result.alternatives.mapIndexed { index, alternative ->
            val chatMessage = ChatMessage(
                role = try {
                    ChatCompletionRole.valueOf(alternative.message.role.lowercase())
                } catch (e: Exception) {
                    ChatCompletionRole.assistant
                },
                content = alternative.message.text
            )

            ChatCompletionChoice(
                index = index,
                message = chatMessage,
                finishReason = null
            )
        }


        val usage = resultResponse.result.usage.inputTextTokens?.let {
            Usage(
                promptTokens = resultResponse.result.usage.inputTextTokens.toLong(),
                completionTokens = resultResponse.result.usage.completionTokens?.toLong() ?: 0L,
                totalTokens = resultResponse.result.usage.totalTokens?.toLong() ?: 0L
            )
        }
        return ChatCompletionResult(
            id = null,
            `object` = null,
            created = System.currentTimeMillis(),
            model = resultResponse.result.modelVersion,
            choices = choices,
            usage = usage
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

