kotlin
class YandexGPTService(apiKey: String)
```

- `apiKey` - ключ доступа к API Yandex.GPT.

### Публичные функции класса

#### `generateText`

Запускает процесс генерации текста на основе переданных указаний.

```kotlin
suspend fun generateText(prompt: String, maxTokens: Int): String
```

- `prompt` - начальный текст, который используется как подсказка для генерации.
- `maxTokens` - максимальное количество токенов (слов/символов) для генерируемого текста.
- Возвращает строку с сгенерированным текстом.

#### `getApiKey`

Возвращает текущий используемый ключ API Yandex.GPT.

```kotlin
fun getApiKey(): String
```

### Приватные функции и элементы класса

#### `httpClient`

Приватный экземпляр HTTP-клиента, используемый для взаимодействия с API.

```kotlin
private val httpClient: HttpClient
```

#### `makeRequest`

Создает и выполняет запрос к API Yandex.GPT для получения генерированного текста.

```kotlin
private suspend fun makeRequest(payload: RequestPayload): Response
```

- `payload` - данные для запроса в формате определенного объекта запроса.
- Возвращает объект ответа, содержащий сгенерированный текст.

## Примеры использования

```kotlin
// Создание экземпляра сервиса с ключом API
val yandexGPTService = YandexGPTService("YOUR_API_KEY")

// Генерация текста с указанием начальной фразы и ограничения по количеству токенов
val generatedText = yandexGPTService.generateText("Пример начального текста", 100)

// Вывод сгенерированного текста на экран
println(generatedText)
