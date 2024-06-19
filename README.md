# Класс YandexGPTService

## Описание класса

`YandexGPTService` - это класс, предназначенный для осуществления взаимодействия с API GPT (Generative Pre-trained Transformer) от Yandex. Класс предоставляет методы для отправки запросов на генерацию текста, анализа полученных ответов и управления параметрами модели.

## Состав класса

### Константы

- `API_URL` - URL для доступа к Yandex GPT API.

### Поля

- `apiKey` - ключ доступа к API.
- `httpClient` - экземпляр `HttpClient` для выполнения HTTP-запросов.

### Конструкторы

- `constructor(apiKey: String)` - конструктор, который принимает ключ доступа к API и инициализирует HTTP клиент.

### Функции

- `generateText(prompt: String, params: Map<String, Any> = emptyMap()): String` - функция для отправки запроса на генерацию текста. Принимает текст запроса и необязательный словарь с дополнительными параметрами.
- `parseResponse(response: String): GPTResponse` - функция для анализа ответа от API. Возвращает объект с результатами генерации.
- `handleError(error: String): Unit` - функция для обработки ошибок API.

## Примеры использования

```kotlin
// Инициализация сервиса с ключом доступа к API
val yandexGPTService = YandexGPTService("your_api_key")

// Запрос на генерацию текста с заданным текстовым запросом
val prompt = "Привет, как дела?"
val generatedText = yandexGPTService.generateText(prompt)

// Вывод сгенерированного текста в консоль
println(generatedText)
