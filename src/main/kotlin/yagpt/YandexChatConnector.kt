package yagpt

import com.mlp.sdk.utils.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


/*
 * Константы для аутентификации YandexGPT
 */
private const val URL_YANDEX_COMPLETION = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion"
private val MEDIA_TYPE_JSON = "application/json".toMediaType()
private const val TOKEN_EXPIRATION_DURATION = 12 * 60 * 60 * 1000

/*
 Отслеживание последнего сообщения для partialResponse
 */
var isFirstMessage = false
var isLastMessage = false


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


/*
 * Дата класс для запроса логирования Athina синхронный
 */
data class AthinaApiRequest(
    val language_model_id: String,
    val prompt: Prompt,
    val response: YandexChatResponse
)

data class Prompt(
    val role: String,
    val content: String
)

/*
 * Дата класс для получения ответа от Athina
 */
data class AthinaApiResponse(
    val status: String?,
    val data: Data?
)

data class Data(
    val prompt_run_id: String
)

class YandexChatConnector(val initConfig: InitConfig) {
    private val httpClient = OkHttpClient()
    private val athinaClient = OkHttpClient()

    private var tokenExpirationTime: Long = 0L
    private var iamToken: String = ""
    private var count: Int = 0


    /*
     * Отправление запроса на сервер Yandex
     */
    fun sendMessageToYandex(yandexReq: YandexChatRequest): YandexChatResponse {
        updateIamToken()

        val request = Request.Builder()
            .url(URL_YANDEX_COMPLETION)
            .header("Authorization", "Bearer $iamToken")
            .header("x-folder-id", initConfig.xFolderId)
            .post(JSON.stringify(yandexReq).toRequestBody(MEDIA_TYPE_JSON))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code ${response.code}")
        }

        val result = JSON.parse(response.body!!.string(), YandexChatResponse::class.java)
        println()
        println("________________")
        println(result)
        println("________________")
        println()
        return result
    }

    suspend fun sendMessageToYandexAsync(yandexReq: YandexChatRequest, callback: (YandexChatResponse) -> Unit) {
        updateIamToken()

        val emptyResponse = YandexChatResponse(
            alternatives = listOf(
                Alternatives(
                message = Message(
                    role = "",
                    text = ""
                ),
                status = ""
            ),
            ),


            usage = Usage(
                inputTextTokens = "",
                totalTokens = "",
                completionTokens = ""
            ),
            modelVersion = ""
        )


        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(URL_YANDEX_COMPLETION)
                .header("Authorization", "Bearer $iamToken")
                .header("x-folder-id", initConfig.xFolderId)
                .post(JSON.stringify(yandexReq).toRequestBody(MEDIA_TYPE_JSON))
                .build()

            try {
                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.invoke(emptyResponse)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            if (response.isSuccessful) {
                                val result =
                                    JSON.parse(response.body!!.string(), YandexChatResponse::class.java)

                                isFirstMessage = count == 0
                                count++
                                callback.invoke(result)
                            } else {
                                isLastMessage = true
                                callback.invoke(emptyResponse)
                            }
                        } catch (e: IOException) {
                            callback.invoke(emptyResponse)
                        }
                        isLastMessage = false
                        isFirstMessage = false
                        count = 0
                    }
                })
            } catch (e: Exception) {
                callback.invoke(emptyResponse)
            }
        }
    }

    fun sendLogsToAthina(request: YandexChatRequest, response: YandexChatResponse): AthinaApiResponse {
        val body = AthinaApiRequest(
            language_model_id = "",
            prompt = Prompt(
                role = request.messages.first().role ?: "user",
                content = request.messages.first().text ?: ""
            ),
            response = response
        )

        val request = Request.Builder()
            .url("https://log.athina.ai/api/v1/log/inference")
            .header("Content-Type", "application/json")
            .header("athina-api-key", "UMY16LUIYnMlnYKaEqhhBWs8HZiDdgA9")
            .post(JSON.stringify(body).toRequestBody(MEDIA_TYPE_JSON))
            .build()

        val response = athinaClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code ${response.code}")
        }
        return JSON.parse(response.body!!.string(), AthinaApiResponse::class.java)
    }

    /*
         * Обновление IAM токена
         */
    private fun updateIamToken() {
        if (tokenExpirationTime >= System.currentTimeMillis() || iamToken.isNullOrEmpty()) {
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