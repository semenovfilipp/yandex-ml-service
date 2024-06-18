val yandexGPTService = YandexGPTService("ВАШ_ЗДЕСЬ_API_KEY")
```

### Запрос на генерацию текста

```kotlin
val prompt = "Главные новости дня:"
val maxTokens = 50 // Количество токенов, которое мы хотим сгенерировать

val generatedText = yandexGPTService.generatePrompt(prompt, maxTokens)

println(generatedText)
