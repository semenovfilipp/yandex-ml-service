# Документация класса `YandexGPTService`

## Описание класса

Класс `YandexGPTService` предназначен для работы с API Яндекса, использующим технологию GPT для генерации текста. Класс облегчает взаимодействие с сервисом: отправку запросов и обработку ответов.

## Состав класса

### Свойства класса

- `apiToken` - переменная для хранения токена доступа к API.
- `apiUrl` - URL адрес API Яндекс GPT сервиса.

### Функции класса

- `constructor(apiToken: String)` - конструктор класса, инициализирующий объект с заданным токеном доступа.
- `generateText(prompt: String, params: Map<String, Any>): String` - функция для генерации текста на основе переданного текстового запроса (prompt) и дополнительных параметров. Возвращает сгенерированный текст.
- `parseResponse(response: String): String` - вспомогательная функция для обработки ответа от API и извлечения сгенерированного текста.

## Примеры использования

### Создание экземпляра класса

```kotlin
val apiToken = "ваш_api_токен"
val yandexGPTService = YandexGPTService(apiToken)
```

### Генерация текста

```kotlin
val prompt = "Привет, как дела?"
val params = mapOf("length" to 100, "creativity" to 0.5)
val generatedText = yandexGPTService.generateText(prompt, params)
println(generatedText)
```

### Обработка ответа от сервиса

```kotlin
val response = "{...}"  // предполагаемый JSON ответ от API
val parsedText = yandexGPTService.parseResponse(response)
println(parsedText)
```

## Примечания

- Все примеры являются иллюстративными, и их следует адаптировать с учетом реальной реализации класса и спецификации API.
- Не забудьте обработать исключения и ошибки API в реальной реализации класса.
- Убедитесь в соблюдении требований по безопасности данных, особенно при работе с токенами и личной информацией.

