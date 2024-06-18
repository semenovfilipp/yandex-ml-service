val yandexGPTService = YandexGPTService()
yandexGPTService.setApiKey("ваш_ключ_api")
```

### Генерация текста

```kotlin
// Запрос на генерацию текста
val prompt = "Привет, как твои дела?"
val maxLength = 50

// Вызов метода для генерации текста
val generatedText = yandexGPTService.generateText(prompt, maxLength)
println(generatedText)
```

При вызове функции `generateText` передаётся подсказка `prompt`, на основе которой будет сгенерирован текст, и максимальная длина результата `maxLength`.

Обращаем ваше внимание на то, что представленная документация является допущением и может значительно отличаться от реального класса и его реализации. Используйте этот образец как основу для написания фактической документации для вашего класса `YandexGPTService`.

