fun main() {
    // Инициализируем сервис с ключом API
    val gptService = YandexGPTService(apiKey = "ВАШ_КЛЮЧ_API")

    // Запускаем генерацию текста
    gptService.generateText(prompt = "Пример начального текста") { result ->
        // Выводим результат
        println("Сгенерированный текст: $result")
    }
}
