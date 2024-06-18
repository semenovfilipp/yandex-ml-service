fun main() {
    val service = YandexGPTService()
    val token = service.getToken()

    if (Utils.validateToken(token)) {
        val session = service.createSession(token)
        val text = service.generateText(session, "Пример начального текста", 100)
        println(text)
        service.closeSession(session)
    } else {
        println("Неверный токен аутентификации")
    }
}
```

## Установка и конфигурация

Чтобы использовать `YandexGPTService`, необходимо добавить зависимость в `build.gradle` файл вашего Kotlin проекта.

## Лицензионное соглашение

Данный сервис предоставляется по лицензии MIT, что позволяет использовать, копировать, изменять, сливать, публиковать, распространять, предоставлять в сублицензию и/или продавать копии ПО.

## Контакты

Если у вас возникли вопросы по использованию `YandexGPTService`, пожалуйста, свяжитесь с нами по адресу [support@example.com](mailto:support@example.com).
