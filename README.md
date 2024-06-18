val service = YandexGPTService()
val token = service.requestToken()
val options = GenerationOptions(maxTokens = 100, temperature = 0.5, topP = 0.9)
val prompt = "Введение в README файла следует начать с описания..."
val generatedText = service.generateText(prompt, options)
println(generatedText)
```

При использовании данного примера сервис сгенерирует продолжение текста, начинающегося со строки `prompt`.

## Контактная информация

Для получения дополнительной помощи или в случае возникновения вопросов вы можете обратиться к следующим контактным данным:

- E-mail: support@yandex-service.com
- Телефон: +7 (123) 456-78-90

