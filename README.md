kotlin
// Инициализация сервиса с API ключом
val gptService = YandexGPTService(apiKey = "your-api-key")

// Задаем параметры для генерации текста
val params = GPTParameters(
    prompt = "Пример текста для продолжения",
    maxTokens = 100,
    temperature = 0.5
)

// Вызов метода для генерации текста
val generatedText = gptService.generateText(params)
println(generatedText)
```

#### Получение ответа на вопрос

```kotlin
// Инициализация сервиса с API ключом
val gptService = YandexGPTService(apiKey = "your-api-key")

// Вводим вопрос и контекст
val question = "Какое самое большое животное в мире?"
val context = "Синий кит - самое крупное животное, живущее сегодня на Земле."

// Вызов метода для получения ответа на вопрос
val answer = gptService.answerQuestion(question, context)
println(answer)
