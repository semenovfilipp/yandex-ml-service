kotlin
val yandexGPTService = YandexGPTService(apiToken = "ваш_api_токен")

val payload = ConversePayload(
    query = "Какой сегодня день?"
)

val response = yandexGPTService.converse(payload)
println(response.text) // Выведет ответ на заданный вопрос
```

#### Использование функции `generateText`

```kotlin
val yandexGPTService = YandexGPTService(apiToken = "ваш_api_токен")

val prompt = "Примеры использования искусственного интеллекта в повседневной жизни: "
val generatedText = yandexGPTService.generateText(prompt, maxTokens = 50)
println(generatedText) // Выведет сгенерированный текст на основе заданного начала
