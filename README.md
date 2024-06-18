val yandexGPTService = YandexGPTService()
yandexGPTService.authenticate("ваш_api_ключ")
val generatedText = yandexGPTService.generateText("Пример подсказки", 100)
println(generatedText)
```

### Исключения
Классы исключений, используемые для обработки ошибок при работе с Yandex GPT.

- `class AuthenticationException(message: String)`
  Исключение, выбрасываемое при неудачной аутентификации.

- `class GenerationException(message: String)`
  Исключение, выбрасываемое при ошибках генерации текста.

## Примеры использования
Предположим, вы хотите использовать `YandexGPTService` для генерации текстового контента. Пример простой программы на Kotlin:

```kotlin
fun main() {
    try {
        val yandexGPTService = YandexGPTService()
        if (yandexGPTService.authenticate("ваш_api_ключ")) {
            val text = yandexGPTService.generateText("Начало вашего текста", 200)
            println(text)
        } else {
            println("Ошибка аутентификации. Проверьте ваш API ключ.")
        }
    } catch (e: Exception) {
        println("Произошла ошибка: ${e.message}")
    }
}
```

## Зависимости
Перечислите все библиотеки и фреймворки, которые требуются для работы сервиса,
например, библиотека для работы с HTTP-запросами или JSON.

## Установка
Опишите процесс установки и настройки вашего сервиса.

## Лицензия
Укажите информацию о лицензии, под которой распространяется сервис.

---

Note: Этот образец README.md предполагает, что исходный код содержится в указанном файле и что классы и функции в нем относятся к функционалу сервиса взаимодействия с Yandex GPT API. В реальности содержание вашего файла может отличаться.
