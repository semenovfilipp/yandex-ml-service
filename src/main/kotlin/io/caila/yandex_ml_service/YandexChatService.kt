package io.caila.yandex_ml_service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mlp.sdk.datatypes.chatgpt.ChatCompletionRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException



/*
 * Дата классы для принятия JSON из YandexGPT
 */
data class ResultResponse(val result: io.caila.yandex_ml_service.Result)
data class Result(val alternatives: List<Alternatives>, val usage: Usage, val modelVersion : String)
data class Message(val role: String, val text: String)
data class Alternatives(val message: Message, val status : String)
data class Usage(val inputTextTokens : String?, val completionTokens: String?, val totalTokens: String?)



/*
 * Сервисные конфигурации для доступа к YandexGPT
 */

data class InitConfig(
    val iAmToken: String = "t1.9euelZqKx8qdjZCQlsuZl5KXmZzMi-3rnpWazZ3LnpyOz8qdlo-dzJPJlpTl9PdQRjdR-e9PXRbQ3fT3EHU0UfnvT10W0M3n9euelZrPlZLJi8iTzprKyMyQx42JlO_8xeuelZrPlZLJi8iTzprKyMyQx42JlA.PfPCz2dpZfqHLGlQcjHNle6-fPCGqVYsk7XJ8YynB3_ZQSsYqhVE400OE02RDF1bIYG3KxPS5b_gbbuMQtBbDA",
    val xFolderId: String = "b1gqi77kftnmedl1qn05",
    val modelUri: String = "gpt://b1gqi77kftnmedl1qn05/yandexgpt-lite"
)

/*
 * Опциональные конфигурации для настройки запроса к YandexGPT
 */
data class PredictConfig(
    val maxTokens: String = "2000",
    val temperature: Double = 0.6,
    val stream: Boolean = false
)


class YandexChatService() {
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
                "role" to message.role.toString().lowercase(),
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

    fun sendMessageToYandex(req: ChatCompletionRequest): ResultResponse {
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
            message = message,
            status = alternativesNode?.get("status")?.asText() ?: ""
        )

        val usage = Usage(
            inputTextTokens = usageNode?.get("inputTextTokens")?.asText() ?: "",
            completionTokens = usageNode?.get("completionTokens")?.asText() ?: "",
            totalTokens = usageNode?.get("totalTokens")?.asText() ?: ""
        )

        val result = Result(
            alternatives = listOf(alternative),
            usage = usage,
            modelVersion = modelVersion
        )

        return ResultResponse(result)
    }
}


