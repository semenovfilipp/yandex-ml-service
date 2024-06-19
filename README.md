# Документация класса `YandexGPTService`

## Описание класса

`YandexGPTService` - это класс, предоставляющий интерфейс для взаимодействия с GPT-моделями от Яндекса. Класс позволяет отправлять запросы на генерацию текста и получение ответов от моделей, обученных с использованием технологий глубокого обучения.

## Состав класса

### Конструкторы

- `YandexGPTService(apiKey: String, model: String)`  
  Создает новый экземпляр сервиса, инициализируя его с API-ключом и моделью для запросов.

### Функции

- `fun generateText(prompt: String, maxTokens: Int): String`  
  Отправляет текстовый запрос (`prompt`) модели GPT и возвращает сгенерированный текст. `maxTokens` определяет максимальное количество токенов в ответе.

- `fun setModel(modelName: String): Boolean`  
  Устанавливает новую модель для генерации текста. Возвращает `true`, если модель успешно изменена.

### Приватные поля

- `private val httpClient: HttpClient`  
  Клиент для выполнения HTTP-запросов.

### Примеры использования

#### Инициализация сервиса

```kotlin
val apiKey = "your_api_key"
val gptService = YandexGPTService(apiKey, "text-generation")
```

#### Генерация текста

```kotlin
val prompt = "Пример текста для генерации"
val generatedText = gptService.generateText(prompt, 100)
println(generatedText)
```

#### Смена модели

```kotlin
val newModel = "text-generation-premium"
val isModelChanged = gptService.setModel(newModel)
println("Модель изменена: $isModelChanged")
```

---

Для более точной документации нужно видеть реальную структуру и код класса `YandexGPTService`, так как содержание документации напрямую зависит от реализации класса.
