package io.caila.yandex_ml_service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionRequest
import com.mlp.sdk.utils.JSON
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


/*
 * Дата классы для принятия JSON из YandexGPT
 */
data class ResultResponse(val result: Result)
data class Result(val alternatives: List<Alternatives>, val usage: Usage, val modelVersion: String)
data class Message(val role: String, val text: String)
data class Alternatives(val message: Message, val status: String)
data class Usage(val inputTextTokens: String?, val completionTokens: String?, val totalTokens: String?)


/*
 * Сервисные конфигурации для доступа к YandexGPT
 */

data class InitConfig(var iAmToken: String, val xFolderId: String, val modelUri: String, val oauthToken: String)

/*
 * Опциональные конфигурации для настройки запроса к YandexGPT
 */
data class PredictConfig(
    val maxTokens: String = "2000", val temperature: Double = 0.6, val stream: Boolean = false
)


class YandexChatService {
    private val predictConfig = PredictConfig()

    private val httpClient = OkHttpClient()
    private val objectMapper = ObjectMapper()

    private var tokenExpirationTime: Long = System.currentTimeMillis() + 12 * 60 * 60 * 1000

    private val initConfig = JSON.parse(System.getenv().get("SERVICE_CONFIG") ?: "{}", InitConfig::class.java)


    /*
     * Создание JSON
     */

    private fun createRequestBody(req: ChatCompletionRequest): String {
        val messages = req.messages.map { message ->
            mapOf(
                "role" to message.role.toString().lowercase(),
                "text" to message.content
            )
        }
        val maxTokens = req.maxTokens ?: predictConfig.maxTokens
        val temperature = req.temperature ?: predictConfig.temperature
        val stream = req.stream ?: predictConfig.stream


        val jsonBody = mapOf(
            "modelUri" to initConfig.modelUri, "completionOptions" to mapOf(
                "stream" to stream,
                "temperature" to temperature,
                "maxTokens" to maxTokens
            ), "messages" to messages
        )

        return objectMapper.writeValueAsString(jsonBody)
    }

    /*
     * Создание HTTP-запроса с заголовками и телом запроса
     */
    private fun createRequest(requestBody: String): Request {
        return Request.Builder().url("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
            .header("Authorization", "Bearer ${initConfig.iAmToken}").header("x-folder-id", initConfig.xFolderId)
            .post(requestBody.toRequestBody("application/json".toMediaType())).build()
    }

    /*
     *  Отправление запроса на сервер Yandex
     */

    fun sendMessageToYandex(req: ChatCompletionRequest): ResultResponse {
        updateIamToken()
        val requestBody = createRequestBody(req)
        val request = createRequest(requestBody)

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }

        val responseData = response.body?.string()
        return parseCompletionText(responseData)
    }

    /*
     * Разбор JSON ответа
     */
    private fun parseCompletionText(responseData: String?): ResultResponse {
        val rootNode: JsonNode = objectMapper.readTree(responseData)
        val alternativesNode = rootNode["result"].get("alternatives").firstOrNull()
        val usageNode = rootNode["result"].get("usage")
        val modelVersion = rootNode["result"].get("modelVersion").asText() ?: ""

        val message = Message(
            role = alternativesNode?.get("message")?.get("role")?.asText() ?: "",
            text = alternativesNode?.get("message")?.get("text")?.asText() ?: ""
        )

        val alternative = Alternatives(
            message = message, status = alternativesNode?.get("status")?.asText() ?: ""
        )

        val usage = Usage(
            inputTextTokens = usageNode?.get("inputTextTokens")?.asText() ?: "",
            completionTokens = usageNode?.get("completionTokens")?.asText() ?: "",
            totalTokens = usageNode?.get("totalTokens")?.asText() ?: ""
        )

        val result = Result(
            alternatives = listOf(alternative), usage = usage, modelVersion = modelVersion
        )

        return ResultResponse(result)
    }

    /*
     * Обновление iamToken каждые 12 часов
     */
    private fun updateIamToken() {
        if (System.currentTimeMillis() >= tokenExpirationTime || initConfig.iAmToken == "") {
            val newToken = getNewIamToken(initConfig.oauthToken)
            initConfig.iAmToken = newToken
            tokenExpirationTime = System.currentTimeMillis() + 12 * 60 * 60 * 1000
        }
    }

    /*
     * Запрос на обновление iamToken
     */
    private fun getNewIamToken(oauthToken: String): String {
        val client = OkHttpClient()
        val requestBody = "{\"yandexPassportOauthToken\":\"$oauthToken\"}"
        val request = Request.Builder()
            .url("https://iam.api.cloud.yandex.net/iam/v1/tokens")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Failed to retrieve new IAM token: ${response.code} - ${response.message}")
        }

        val responseData = response.body?.string()
        return parseNewToken(responseData)
    }


    /*
     * Парсинг полученного iamToken
     */
    private fun parseNewToken(responseData: String?): String {
        responseData ?: throw IOException("Failed to parse new IAM token: response data is null")

        val objectMapper = ObjectMapper()
        val rootNode = objectMapper.readTree(responseData)

        return rootNode["iamToken"]?.asText()
            ?: throw IOException("Failed to parse IAM token from response")
    }
}


