# YandexGPTService

## Описание класса

`YandexGPTService` - класс предназначен для работы с API Yandex GPT (Generative Pre-trained Transformer), который обеспечивает взаимодействие с системой Yandex на основе машинного обучения для генерации текста. Данный класс может использоваться для отправки запросов к Yandex GPT и получения ответов, генерации текстовых предложений и сценариев.

## Состав класса

### Свойства класса

- `apiKey: String` - свойство для хранения ключа API, необходимого для аутентификации запросов.

### Конструкторы класса

- `constructor(apiKey: String)` - конструктор для инициализации ключа API.

### Функции класса

- `fun sendRequest(prompt: String, maxTokens: Int): String` - функция для отправки текстового запроса API и получения ответа. Принимает на вход начальный текст (`prompt`) и максимальное количество токенов для генерации (`maxTokens`).
- `fun parseResponse(response: String): String` - функция для разбора полученного ответа и извлечения сгенерированного текста.

## Примеры использования

### Инициализация класса с ключом API

```kotlin
val yandexGPTService = YandexGPTService("your_api_key_here")
```

### Отправка запроса и получение ответа

```kotlin
val prompt = "Пример текста для начала генерации"
val maxTokens = 100
val generatedText = yandexGPTService.sendRequest(prompt, maxTokens)
println(generatedText)
```

### Обработка ответа от API

```kotlin
val jsonResponse = "{ \"responses\": [ { \"text\": \"сгенерированный текст\" } ] }"
val text = yandexGPTService.parseResponse(jsonResponse)
println(text)
```

## Замечания

Описанная выше документация к `YandexGPTService` является лишь шаблоном и должна быть адаптирована в соответствии с реальной реализацией класса и методами API Yandex GPT.

Не забудьте заменить "your_api_key_here" на действующий ключ API, предоставляемый Yandex для доступа к их сервису GPT.
