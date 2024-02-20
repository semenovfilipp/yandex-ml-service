package yagpt

import com.mlp.sdk.*
import com.mlp.sdk.datatypes.chatgpt.*
import com.mlp.sdk.datatypes.chatgpt.Usage
import com.mlp.sdk.utils.JSON

/*
 * Сервисные конфигурации для доступа к YandexGPT
 */

data class InitConfig(val xFolderId: String, val modelUri: String, val oauthToken: String)

/*
 * Опциональные конфигурации для настройки запроса к YandexGPT
 */
data class PredictConfig(
    val systemPrompt: String? = null,

    val maxTokens: Int = 2000,
    val temperature: Double = 0.6,
    val stream: Boolean = false
)

class YandexGPTService(
    override val context: MlpExecutionContext
) : MlpPredictWithConfigServiceBase<ChatCompletionRequest, PredictConfig, ChatCompletionResult>(
    REQUEST_EXAMPLE,
    PredictConfig(),
    RESPONSE_EXAMPLE
) {

    private val initConfig = JSON.parse(System.getenv()["SERVICE_CONFIG"] ?: "{}", InitConfig::class.java)
    private val defaultPredictConfig = PredictConfig()

    private val connector = YandexChatConnector(initConfig)

    override fun predict(request: ChatCompletionRequest, config: PredictConfig?): ChatCompletionResult {
        val yandexChatRequest = YandexChatRequest(
            modelUri = initConfig.modelUri,
            completionOptions = YandexChatCompletionOptions(
                maxTokens = request.maxTokens ?: config?.maxTokens ?: defaultPredictConfig.maxTokens,
                temperature = request.temperature ?: defaultPredictConfig.temperature,
                stream = false
            ),
            messages =
            listOf(
                YandexChatMessage(
                    role = if (defaultPredictConfig.systemPrompt!=null)  ChatCompletionRole.system.toString() else request.messages.first().role.toString() ,
                    text = request.messages.first().content
                )
            )
        )

        val resultResponse = connector.sendMessageToYandex(yandexChatRequest)

        val choices = resultResponse.alternatives.mapIndexed { index, alternative ->
            val chatMessage = ChatMessage(
                role = ChatCompletionRole.assistant,
                content = alternative.message.text
            )

            ChatCompletionChoice(
                index = index,
                message = chatMessage,
                finishReason = null
            )
        }

//        BillingUnitsThreadLocal.setUnits(...)

        val usage = resultResponse.usage.inputTextTokens?.let {
            Usage(
                promptTokens = resultResponse.usage.inputTextTokens.toLong(),
                completionTokens = resultResponse.usage.completionTokens?.toLong() ?: 0L,
                totalTokens = resultResponse.usage.totalTokens?.toLong() ?: 0L
            )
        }
        return ChatCompletionResult(
            id = null,
            `object` = null,
            created = System.currentTimeMillis(),
            model = resultResponse.modelVersion,
            choices = choices,
            usage = usage
        )
    }


    companion object {
        val REQUEST_EXAMPLE = ChatCompletionRequest(
            messages = listOf(
                ChatMessage(ChatCompletionRole.user, "What is Kotlin")

            )
        )
        val RESPONSE_EXAMPLE = ChatCompletionResult(
            model = "yandex-gpt-lite",
            choices = listOf(
                ChatCompletionChoice(
                    message = ChatMessage(
                        role = ChatCompletionRole.assistant,
                        content = "Kotlin is an island"
                    ),
                    index = 11
                )
            ),
        )
    }
}

fun main() {
    val actionSDK = MlpServiceSDK({ YandexGPTService(MlpExecutionContext.systemContext) })

    actionSDK.start()
    actionSDK.blockUntilShutdown()
}
