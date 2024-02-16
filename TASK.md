
1. Вэб сервис Caila формирует JSON
2. Этот JSON состоит из:

```
{
  "messages": [
    {
      "role": "user",
      "content": "Привет, как дела?"
    }
    // Другие сообщения...
  ]
}
```


YANDEX JSON
```
{
  "modelUri": "gpt://<идентификатор_каталога>/yandexgpt-lite",
  "completionOptions": {
    "stream": false,
    "temperature": 0.6,
    "maxTokens": "2000"
  },
  "messages": [
    {
      "role": "system",
      "text": "Найди ошибки в тексте и исправь их"
    },
    {
      "role": "user",
      "text": "Ламинат подойдет для укладке на кухне или в детской комнате – он не боиться влаги и механических повреждений благодаря защитному слою из облицованных меламиновых пленок толщиной 0,2 мм и обработанным воском замкам."
    }
  ]
}
```
3. JSON придет в виде объекта ChatCompletionRequest
4. Из объекта нужно забрать  messages
   Надо забрать
- role (getRole), который может быть “user” или “system”
- content (getContent) , который мы вставим в text

5. Далее полученные поля из объекта ChatCompletionRequest надо поместить в новый JSON объект, который отправится в Yandex
- role (getRole из объекта ChatCompletionRequest )
- text (getContent из объекта ChatCompletionRequest)

6. У нас есть initConfig, который содержит
- modelUri
- IAM token
- идентификатор каталога

7. Также есть predictConfig, который обычно содержит опциональные параметры
- temperature
- maxTokens
- stream равный false

8. В JSON который отправиться в Yandex нужно добавить заголовки
- Authorization , который возьмем из initConfig
- x-folder-id, который возьмем из initConfig
- Content-Type: application/json

9. Далее мы отправляем на Yandex JSON
   Приблизительно такой
   {
   "modelUri": "gpt://<идентификатор_каталога>/yandexgpt-lite",
   "completionOptions": {
   "stream": false,
   "temperature": 0.6,
   "maxTokens": "2000"
   },
   "messages": [
   {
   "role": "system",
   "text": "Найди ошибки в тексте и исправь их"
   },
   {
   "role": "user",
   "text": "Ламинат подойдет для укладке на кухне или в детской комнате – он не боиться влаги и механических повреждений благодаря защитному слою из облицованных меламиновых пленок толщиной 0,2 мм и обработанным воском замкам."
   }
   ]
   }

10. Отправляется он из терминала таким образом, но нам нужно отправить из нашего сервиса

export FOLDER_ID=<идентификатор_каталога>
export IAM_TOKEN=<IAM-токен>
curl --request POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer ${IAM_TOKEN}" \
-H "x-folder-id: ${FOLDER_ID}" \
-d "@prompt.json" \
"https://llm.api.cloud.yandex.net/foundationModels/v1/completion"

11. Yandex отправляет ответ  в сервис  в виде JSON в таком.
12. Из JSON мы забираем из message значение text
```
{
  "result": {
    "alternatives": [
      {
        "message": {
          "role": "assistant",
          "text": "Ламинат подходит для укладки на кухне и в детской комнате. Он не боится влажности и механических повреждений, благодаря защитному слою, состоящему из меланиновых плёнок толщиной 0.2 мм, и обработанным воском замкам."
        },
        "status": "ALTERNATIVE_STATUS_TRUNCATED_FINAL"
      }
    ],
```
13. Перекладываем значение в объект класса ChatCompletionResult
14. Отправляем объект в вэб сервис






/*
Задачи:
1. Сделать обработку получаемых role (user, system)
*/