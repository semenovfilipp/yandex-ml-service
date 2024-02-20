import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.caila.yandex_ml_service.*
import io.caila.yandex_ml_service.exception.ParsingException


/*
 * Парсинг JSON полученного из YandexGPT
 */
object YandexResponseParser {
    private val objectMapper = ObjectMapper()

    fun parseResponse(responseData: String?): ResultResponse {
        responseData ?: throw IllegalArgumentException("Response data is null")

        try {
            val rootNode: JsonNode = objectMapper.readTree(responseData)
            val resultNode = rootNode["result"]
                ?: throw IllegalArgumentException("Result node not found in response")

            val alternativesNode = resultNode.get("alternatives").firstOrNull()
                ?: throw IllegalArgumentException("Alternatives node not found in response")
            val usageNode = resultNode.get("usage")

            val message = Message(
                role = alternativesNode["message"]?.get("role")?.asText() ?: "",
                text = alternativesNode["message"]?.get("text")?.asText() ?: ""
            )

            val alternative = Alternatives(
                message = message,
                status = alternativesNode["status"]?.asText() ?: ""
            )

            val usage = Usage(
                inputTextTokens = usageNode?.get("inputTextTokens")?.asText() ?: "",
                completionTokens = usageNode?.get("completionTokens")?.asText() ?: "",
                totalTokens = usageNode?.get("totalTokens")?.asText() ?: ""
            )

            val modelVersion = resultNode.get("modelVersion")?.asText() ?: ""

            val result = Result(
                alternatives = listOf(alternative),
                usage = usage,
                modelVersion = modelVersion
            )

            return ResultResponse(result)
        } catch (e: Exception) {
            throw ParsingException("Failed to parse JSON response from YandexGPT", e)
        }
    }
}
