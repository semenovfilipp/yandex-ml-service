kotlin
class YandexGPTService(apiKey: String, serviceUrl: String) { ... }
```

### Функции

#### initSession

Инициализирует новую сессию взаимодействия с Яндекс GPT. Возможно, это потребуется для установления контекста диалога или начала новой последовательности запросов.

```kotlin
fun initSession(): Session { ... }
```

#### generateText

Отправляет текст запрос для генерации с помощью модели GPT. Возвращает сгенерированный текст в ответе.

```kotlin
fun generateText(inputText: String, sessionId: String?): GeneratedText { ... }
```

#### getSessionInfo

Получает информацию о текущей сессии, возможно включая состояние контекста диалога, счетчики запросов и иные метаданные.

```kotlin
fun getSessionInfo(sessionId: String): SessionInfo { ... }
```

#### closeSession

Закрывает текущую сессию. Это может быть полезно для освобождения ресурсов на стороне Яндекс GPT API и поддержания лимитов сессий.

```kotlin
fun closeSession(sessionId: String): Boolean { ... }
```

### Примеры использования функций

#### Инициализация сессии

```kotlin
val gptService = YandexGPTService(apiKey = "your_api_key", serviceUrl = "https://api-url")
val session = gptService.initSession()
```

#### Генерация текста

```kotlin
val generatedTextResult = gptService.generateText("Пример вводного текста", session.id)
println(generatedTextResult.text)
```

#### Получение информации о сессии

```kotlin
val sessionInfo = gptService.getSessionInfo(session.id)
println(sessionInfo)
```

#### Завершение сессии

```kotlin
val isClosed = gptService.closeSession(session.id)
if (isClosed) {
    println("Сессия успешно закрыта")
}
