import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object YandexRequestBuilder {
    private val MEDIA_TYPE_JSON = "application/json".toMediaType()

    fun buildRequest(url: String, iAmToken: String, xFolderId: String, requestBody: String): Request {
        val requestBodyObject = requestBody.toRequestBody(MEDIA_TYPE_JSON)
        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $iAmToken")
            .header("x-folder-id", xFolderId)
            .post(requestBodyObject)
            .build()
    }
}
