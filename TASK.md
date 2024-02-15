Делаем ML-сервис для доступа к yandex-gpt и gigachat.

Метод predict должен принимать тип ChatCompletionRequest
и возвращать ChatCompletionResult (они определены в mlp-sdk).
Тип параметра predictConfig будет собственным (т.е. будет определён в самом сервисе).

В initConfig должны передаваться ключи доступа в yandex-gpt и gigachat. 
Выглядеть конфиг должен примерно так:
{
"yandex-gpt": {
"api-key": ....
}
"gigachat": {
"api-key": ....
}
}
т.е. параметры для разных моделей пусть будут определяться в разных секциях.