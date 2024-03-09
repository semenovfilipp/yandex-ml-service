package yagpt

import com.mlp.gate.PartialPredictResponseProto
import com.mlp.gate.PayloadProto
import com.mlp.gate.ServiceDescriptorProto
import com.mlp.gate.ServiceToGateProto
import com.mlp.sdk.*
import com.mlp.sdk.datatypes.chatgpt.*
import com.mlp.sdk.datatypes.chatgpt.Usage
import com.mlp.sdk.utils.JSON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.MDC

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

class YandexGPTService : MlpService() {

    private val initConfig = JSON.parse(System.getenv()["SERVICE_CONFIG"] ?: "{}", InitConfig::class.java)
    private val defaultPredictConfig = PredictConfig()
    private val connector = YandexChatConnector(initConfig)

    lateinit var sdk: MlpServiceSDK
    private var connectorId: Long? = null
    private var requestId: Long? = null
    private var priceInNanoTokens: Long = 0L


    override fun predict(req: Payload, conf: Payload?): MlpPartialBinaryResponse {
        val request = JSON.parse(req.data, ChatCompletionRequest::class.java)
        val config = conf?.data?.let { JSON.parse(it, PredictConfig::class.java) }
        val yandexChatRequest = createYandexChatRequest(request, config)

        requestId = MDC.get("gateRequestId").toLong()
        connectorId = MDC.get("connectorId").toLong()


        return if (yandexChatRequest.completionOptions.stream){
            predictAsync(yandexChatRequest)
        }else{
            predictSync(yandexChatRequest)
        }
    }


    /*
     * Асинхронная и синхронная функции для отправки сообщений
     */
    private fun predictAsync(yandexChatRequest: YandexChatRequest): MlpPartialBinaryResponse {
        runBlocking {
            connector.sendMessageToYandexAsync(yandexChatRequest) { yandexChatResponse ->

                val athina = connector.sendLogsToAthina(yandexChatRequest,yandexChatResponse)


                calculateCostForRequest(yandexChatResponse)
                val chatCompletionResponse = createChatCompletionResult(yandexChatResponse)

                val partitionProto = createPartialResponse(chatCompletionResponse)

                println()
                println("__________________________")
                println(partitionProto)
                println(athina)
                println("__________________________")
                println()

                launch {
                    sdk.send(connectorId!!, partitionProto)
                }
            }
        }
        return MlpPartialBinaryResponse()
    }

    private fun predictSync(yandexChatRequest: YandexChatRequest): MlpPartialBinaryResponse {
        val yandexChatResponse = connector.sendMessageToYandex(yandexChatRequest)

        val athinaResponse = connector.sendLogsToAthina(yandexChatRequest, yandexChatResponse)
        val chatCompletionResult = createChatCompletionResult(yandexChatResponse)

        isLastMessage = true
        calculateCostForRequest(yandexChatResponse)
        val partitionProto = createPartialResponse(chatCompletionResult)
        isLastMessage = false

        println()
        println("__________________________")
        println(partitionProto)
        println(athinaResponse)
        println("__________________________")
        println()

        GlobalScope.launch {
            sdk.send(connectorId!!, partitionProto)
            BillingUnitsThreadLocal.setUnits(priceInNanoTokens)
        }

        return MlpPartialBinaryResponse()
    }

    /*
 * Подсчет итоговой стоимости запроса
 */
    fun calculateCostForRequest(response: YandexChatResponse) {
        // TODO Спросить как сделать по аналогии со Сбер и что такое 50
/*        /*GigaChat Lite = 200*/ -> 0,2 рубля за 1000 токенов
//        /*GigaChat Pro = 1500*/ -> 1,5 рубля за 1000 токенов
//        val priceRubPerMillion = if (model == "GigaChat-Pro") 1500 else 200
//        val priceInMicroRoubles = (totalTokens).toLong() * priceRubPerMillion
           priceInNanoTokens = priceInMicroRoubles * 50 * 1000 -> что такое 50
           https://developers.sber.ru/docs/ru/gigachat/api/tariffs -> тарифы сбера
 */
        if(isLastMessage) {
            val totalTokens = (response.usage.totalTokens).toLong()
            val modelCofficient = if (response.modelVersion.equals("YandexGPT")) 5.00 else 1.00
            val pricePerToken = if (response.modelVersion.equals("YandexGPT")) 2.00 else 0.40
            val totalCost = (totalTokens * modelCofficient * (pricePerToken / 1000.0)).toLong()

            priceInNanoTokens = totalCost
        }else{
            priceInNanoTokens = 0L
        }
    }

    /*
     * Создаем объект для отправки в Caila
     */
    fun createPartialResponse(response: Any): ServiceToGateProto {

        return ServiceToGateProto.newBuilder()
            .setRequestId(requestId!!)
            .setPartialPredict(
                PartialPredictResponseProto.newBuilder()
                    .setStart(isFirstMessage)
                    .setFinish(isLastMessage)
                    .setData(
                        PayloadProto.newBuilder()
                            .setJson(JSON.stringify(response))
                            .setDataType("json")
                    )
            )
            .putHeaders("Z-custom-billing", priceInNanoTokens.toString())
            .build()
    }


    private fun createChatCompletionResult(yandexChatResponse: YandexChatResponse): ChatCompletionResult {
        val choices = yandexChatResponse.alternatives.mapIndexed { index, alternative ->
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


        val usage = Usage(
            promptTokens = yandexChatResponse.usage.inputTextTokens?.toLong() ?: 0L,
            completionTokens = yandexChatResponse.usage.completionTokens?.toLong() ?: 0L,
            totalTokens = yandexChatResponse.usage.totalTokens?.toLong() ?: 0L
        )

        return ChatCompletionResult(
            id = null,
            `object` = null,
            created = System.currentTimeMillis(),
            model = yandexChatResponse.modelVersion,
            choices = choices,
            usage = usage
        )
    }

    private fun createYandexChatRequest(
        request: ChatCompletionRequest,
        config: PredictConfig?
    ): YandexChatRequest {
        return YandexChatRequest(
            modelUri = initConfig.modelUri,
            completionOptions = YandexChatCompletionOptions(
                maxTokens = request.maxTokens ?: config?.maxTokens ?: defaultPredictConfig.maxTokens,
                temperature = request.temperature ?: defaultPredictConfig.temperature,
                stream = request.stream ?: defaultPredictConfig.stream
            ),
            messages =
            if (config?.systemPrompt != null) {
                listOf(
                    YandexChatMessage(
                        role = "system",
                        text = config.systemPrompt
                    )
                )
            } else {
                emptyList<YandexChatMessage>() + request.messages.map { message ->
                    YandexChatMessage(
                        role = message.role.toString(),
                        text = message.content
                    )
                }
            }
        )
    }


    /*
     * Необходим для успешного запуска приложения
     */
    override fun getDescriptor(): ServiceDescriptorProto {

        return ServiceDescriptorProto.newBuilder()
            .setName("GigaChat")
            .build()
    }
}

fun main() {
    val service = YandexGPTService()
    val mlp = MlpServiceSDK(service)
    service.sdk = mlp

    mlp.start()
    mlp.blockUntilShutdown()
}

