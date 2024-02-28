package yagpt

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
 * Дата классы запроса в YandexGPT
 */
data class YandexChatRequest(
    val modelUri: String,
    val completionOptions: YandexChatCompletionOptions,
    val messages: List<YandexChatMessage>
)
data class YandexChatCompletionOptions(
    val stream: Boolean,
    val temperature: Double,
    val maxTokens: Int
)
data class YandexChatMessage(
    val role: String,
    val text: String
)

/*
 * Дата классы для принятия JSON из YandexGPT
 */
data class YandexChatResponse(val alternatives: List<Alternatives>, val usage: Usage, val modelVersion: String)
data class Message(val role: String, val text: String)
data class Alternatives(val message: Message, val status: String)
data class Usage(val inputTextTokens: String?, val completionTokens: String?, val totalTokens: String?)


class YandexChatConnector(val initConfig: InitConfig) {
    private val httpClient = OkHttpClient()

    private var tokenExpirationTime: Long = 0L
    private var iamToken: String = ""

    /*
     * Отправление запроса на сервер Yandex
     */
    fun sendMessageToYandex(yandexReq: YandexChatRequest): YandexChatResponse {
        updateIamToken()

        val request = Request.Builder()
            .url(URL_YANDEX_COMPLETION)
            .header("Authorization", "Bearer ${iamToken}")
            .header("x-folder-id", initConfig.xFolderId)
            .post(JSON.stringify(yandexReq).toRequestBody(MEDIA_TYPE_JSON))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code ${response.code}")
        }

        return JSON.parse(response.body!!.string(), YandexChatResponse::class.java)
    }

    /*
     * Обновление IAM токена
     */
    private fun updateIamToken() {
        if (tokenExpirationTime >= System.currentTimeMillis() || iamToken.isEmpty()) {
            val newToken = getNewIamToken(initConfig.oauthToken)
            iamToken = newToken
            tokenExpirationTime = System.currentTimeMillis() + TOKEN_EXPIRATION_DURATION
        }
    }

    /*
     * Получение нового IAM токена
     */
    private fun getNewIamToken(oauthToken: String): String {
        val requestBody = "{\"yandexPassportOauthToken\":\"$oauthToken\"}"
        val request = Request.Builder()
            .url("https://iam.api.cloud.yandex.net/iam/v1/tokens")
            .post(requestBody.toRequestBody(MEDIA_TYPE_JSON))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Failed to retrieve new IAM token: ${response.code} - ${response.message}")
        }

        val responseData = response.body!!.string()
        return JSON.parse(responseData)["iamToken"]!!.asText()
    }
}