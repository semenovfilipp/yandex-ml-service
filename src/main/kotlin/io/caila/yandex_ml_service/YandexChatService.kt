package io.caila.yandex_ml_service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mlp.sdk.MlpPredictServiceBase
import com.mlp.sdk.MlpServiceSDK
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionRequest
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/*
     Задачи:
    1. Сделать обработку получаемых role (user, system)
 */

data class InitConfig(
    val iAmToken: String = "t1.9euelZqTio_Jx5qRxoyNlpKTnZCJyO3rnpWazZ3LnpyOz8qdlo-dzJPJlpTl8_dzeEtR-e9EYU8H_t3z9zMnSVH570RhTwf-zef1656Vms3Iy5eYm4zJkZWanZXPj4nJ7_zF656Vms3Iy5eYm4zJkZWanZXPj4nJ.XsKZeO8AIulap8nWyBZAR3LRIGOdSIP1btOt8UbfZ44hBFGzKqLewnkSLwrFIPuQKDFeOe6BKT-VpeEO_qTrAg",
    val xFolderId: String = "b1gqi77kftnmedl1qn05",
    val modelUri: String = "gpt://b1gqi77kftnmedl1qn05/yandexgpt-lite"
)

data class PredictConfig(
    val maxTokens: String = "2000",
    val temperature: Double = 0.6,
    val stream: Boolean = false
)


class YandexChatService(req: ChatCompletionRequest, response: ChatCompletionResult) :
    MlpPredictServiceBase <ChatCompletionRequest, ChatCompletionResult>(req, response) {
    private val initConfig = InitConfig()
    private val predictConfig = PredictConfig()
    private val httpClient = OkHttpClient()
    private val objectMapper = ObjectMapper()

    /*
     * Создание JSON
     */

    private fun createRequestBody(req: ChatCompletionRequest): String {
        val messages = req.messages.map { message ->
            mapOf(
                "role" to message.role,
                "text" to message.content
            )
        }

        val jsonBody = mapOf(
            "modelUri" to initConfig.modelUri,
            "completionOptions" to mapOf(
                "stream" to predictConfig.stream,
                "temperature" to predictConfig.temperature,
                "maxTokens" to predictConfig.maxTokens
            ),
            "messages" to messages
        )

        return objectMapper.writeValueAsString(jsonBody)
    }

    /*
     * Создание HTTP-запроса с заголовками и телом запроса
     */
    private fun createRequest(requestBody: String): Request {
        return Request.Builder()
            .url("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
            .header("Authorization", "Bearer ${initConfig.iAmToken}")
            .header("x-folder-id", initConfig.xFolderId)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
    }

    /*
     *  Отправление запроса на сервер Yandex
     */

    fun sendMessageToYandex(req: ChatCompletionRequest): String {
        val requestBody = createRequestBody(req)
        val request = createRequest(requestBody)

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }

        val responseData = response.body?.string()
        val completionText = parseCompletionText(responseData)
        return completionText
    }

    /*
     * Разбор JSON ответа
     * Забираем только данные из поля "text"
     */
    private fun parseCompletionText(responseData: String?): String {
        val rootNode: JsonNode = objectMapper.readTree(responseData)
        return rootNode["result"]?.get("alternatives")?.firstOrNull()?.get("message")?.get("text")?.asText() ?: ""
    }

    override fun predict(req: ChatCompletionRequest): ChatCompletionResult {
        val message = sendMessageToYandex(req)
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
    val actionSDK = MlpServiceSDK(YandexChatService::javaClass)
    actionSDK.start()
    actionSDK.blockUntilShutdown()

}