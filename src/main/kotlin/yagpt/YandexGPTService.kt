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
    PREDICT_CONFIG_EXAMPLE,
    RESPONSE_EXAMPLE
) {

    private val initConfig = JSON.parse(System.getenv()["SERVICE_CONFIG"] ?: "{}", InitConfig::class.java)
    private val defaultPredictConfig = PredictConfig()

    private val connector = YandexChatConnector(initConfig)

    override fun predict(request: ChatCompletionRequest, config: PredictConfig?): ChatCompletionResult {

        val messages = mutableListOf<YandexChatMessage>()
        config?.systemPrompt?.let { systemPrompt ->
            messages.add(
                YandexChatMessage(
                    role = "system",
                    text = systemPrompt
                )
            )
        }

        request.messages.forEach { message ->
            messages.add(
                YandexChatMessage(
                    role = message.role.toString(),
                    text = message.content
                )
            )
        }

        val yandexChatRequest = YandexChatRequest(
            modelUri = initConfig.modelUri,
            completionOptions = YandexChatCompletionOptions(
                maxTokens = request.maxTokens ?: config?.maxTokens ?: defaultPredictConfig.maxTokens,
                temperature = request.temperature ?: defaultPredictConfig.temperature,
                stream = false
            ),
            messages =  messages
//            if (config?.systemPrompt!=null){
//                listOf(
//                    YandexChatMessage(
//                        role = "system",
//                        text = config.systemPrompt
//                    )
//                )
//            }else{
//                emptyList<YandexChatMessage>() + request.messages.map { message ->
//                    YandexChatMessage(
//                        role = message.role.toString(),
//                        text = message.content
//                    )
//                }
//            }
        )

            val resultResponse = connector . sendMessageToYandex (yandexChatRequest)

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

        val totalTokens = resultResponse.usage.totalTokens!!.toDouble()
        val totalCost = (totalTokens * 1.0 * (0.40 / 1000.0)).toLong()

        BillingUnitsThreadLocal.setUnits(totalCost)

        val usage = resultResponse.usage.inputTextTokens?.let {
            Usage(
                promptTokens = resultResponse.usage.inputTextTokens.toLong(),
                completionTokens = resultResponse.usage.completionTokens?.toLong() ?: 0L,
                totalTokens = resultResponse.usage.totalTokens.toLong()
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
        val PREDICT_CONFIG_EXAMPLE = PredictConfig(
            systemPrompt = "Верни ответ без гласных",
            maxTokens = 2000,
            temperature = 0.7,
            stream = false,
        )
    }
}

fun main() {
    val actionSDK = MlpServiceSDK({ YandexGPTService(MlpExecutionContext.systemContext) })

    actionSDK.start()
    actionSDK.blockUntilShutdown()
}

