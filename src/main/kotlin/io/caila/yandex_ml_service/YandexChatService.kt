package io.caila.yandex_ml_service

import YandexRequestBuilder
import YandexResponseParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionRequest
import com.mlp.sdk.utils.JSON
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/*
 * Константы для аутентификации YandexGPT
 */
private const val URL_YANDEX_COMPLETION = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion"
private val MEDIA_TYPE_JSON = "application/json".toMediaType()
private const val TOKEN_EXPIRATION_DURATION = 12 * 60 * 60 * 1000

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
    private var tokenExpirationTime: Long = System.currentTimeMillis() + TOKEN_EXPIRATION_DURATION
    private val initConfig = JSON.parse(System.getenv().get("SERVICE_CONFIG") ?: "{}", InitConfig::class.java)
    /*
     * Создание JSON-тела запроса
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
            "modelUri" to initConfig.modelUri,
            "completionOptions" to mapOf(
                "stream" to stream,
                "temperature" to temperature,
                "maxTokens" to maxTokens
            ),
            "messages" to messages
        )

        return objectMapper.writeValueAsString(jsonBody)
    }

    /*
     * Отправление запроса на сервер Yandex
     */
    fun sendMessageToYandex(req: ChatCompletionRequest): ResultResponse {
        updateIamToken()
        val requestBody = createRequestBody(req)
        val request = YandexRequestBuilder.buildRequest(
            URL_YANDEX_COMPLETION,
            initConfig.iAmToken,
            initConfig.xFolderId,
            requestBody
        )

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code ${response.code}")
        }

        val responseData = response.body?.string()
        return YandexResponseParser.parseResponse(responseData)
    }

    /*
     * Обновление IAM токена
     */
    private fun updateIamToken() {
        val expirationTime = System.currentTimeMillis() + TOKEN_EXPIRATION_DURATION

        if (System.currentTimeMillis() >= tokenExpirationTime || initConfig.iAmToken.isEmpty()) {
            val newToken = getNewIamToken(initConfig.oauthToken)
            initConfig.iAmToken = newToken
            tokenExpirationTime = expirationTime
        }
    }

    /*
     * Получение нового IAM токена
     */
    private fun getNewIamToken(oauthToken: String): String {
        try {
            val requestBody = "{\"yandexPassportOauthToken\":\"$oauthToken\"}"
            val request = Request.Builder()
                .url("https://iam.api.cloud.yandex.net/iam/v1/tokens")
                .post(requestBody.toRequestBody(MEDIA_TYPE_JSON))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Failed to retrieve new IAM token: ${response.code} - ${response.message}")
            }

            val responseData = response.body?.string()
            return parseNewToken(responseData)
        } catch (e: IOException) {
            throw RuntimeException("Failed to get new IAM token", e)
        }
    }
    /*
   * Парсинг полученного iamToken
   */
    private fun parseNewToken(responseData: String?): String {
        responseData ?: throw IOException("Failed to parse new IAM token: response data is null")

        val rootNode = objectMapper.readTree(responseData)
        return rootNode["iamToken"]?.asText()
            ?: throw IOException("Failed to parse IAM token from response")
    }
}