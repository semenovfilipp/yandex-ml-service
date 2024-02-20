# Yandex ML Service

Данный ML сервис позволяет:

- Отправить запрос из платформы Caila.io в наше приложение.
- Передавать запрос из приложения в YandexGPT чат.
- Получать ответ из YandexGPT чата в наше приложение.
- Отправить ответ в Caila.io из нашего приложения.

## Этапы

1. **Создание сервиса в Caila.io**
   - Зарегистрируйтесь/войдите в аккаунт на сервисе Caila.io.
   - Перейдите в раздел "Мое пространство" в правом верхнем углу.
   - Нажмите "Создать сервис".
   - В качестве образа выберите "fit-action-example-image".
   - Дайте имя сервису и нажмите "Создать".
   - Далее нажмите на только что созданный сервис.
   - В разделе "Настройки" выберите "Хостинг" -> "Отладочное подключение" -> "Активировать".
   - Перед вами появится графа "env-переменные". Нажмите на нее и сохраните эти переменные в текстовом редакторе. Чуть позже мы рассмотрим их подробнее.

---

Конечно, вот исправленная версия инструкции в формате Markdown:

---

## 2. Создание проекта

- Создайте проект в вашей IDE. В качестве сборщика можно выбрать Gradle или Maven.

### Пример шаблона проекта на Maven
[Ссылка](https://github.com/just-ai/mlp-java-service-template) на шаблон проекта

(Вы также можете найти пример по истории коммитов и на Gradle)

- Подключите зависимость.

#### Для Maven

```xml
<dependency>
    <groupId>com.mlp</groupId>
    <artifactId>mlp-java-sdk</artifactId>
    <version>release-SNAPSHOT</version>
</dependency>
```

#### Для Gradle

```kotlin
dependencies {
    implementation 'com.mlp:mlp-java-sdk:release-SNAPSHOT'
}
```

Чтобы зависимость скачалась, необходимо добавить репозиторий для скачивания этой зависимости.

#### Для Maven

```xml
<repositories>
    <repository>
        <id>nexus-public</id>
        <url>https://nexus-open.just-ai.com/repository/maven-public/</url>
    </repository>
</repositories>
```

#### Для Gradle

```kotlin
repositories {
    maven {
        url 'https://nexus-open.just-ai.com/repository/maven-public/'
    }
}
```

---


Конечно, вот исправленная версия инструкции в формате Markdown для README:

---

### 3. Создаем точку старта нашего приложения

- Создайте класс `Main.kt`.

В этом классе мы будем наследоваться от класса из `mlp-sdk` `MlpPredictServiceBase`.

```kotlin
class Main : MlpPredictServiceBase<ChatCompletionRequest, ChatCompletionResult>()
```

Это нужно нам для того, чтобы иметь доступ к методу `predict`.

```kotlin
 override fun predict(req: ChatCompletionRequest): ChatCompletionResult 
```

Метод `predict` будет получать объект `ChatCompletionRequest` и возвращать `ChatCompletionResult`.

 **ChatCompletionRequest** - объект, который будет приходить к нам в приложение при помощи `Mlp SDK`. В этом объекте нас интересуют поля:

**ChatCompletionResult** - объект, в который мы будем перекладывать полученные данные из JSON от `YandexGPT`. Чуть позже мы рассмотрим, какие поля мы будем передавать.


**ChatCompletionRequest** состоит из:
1. `messages` типа `ChatMessage`. 
2. `ChatMessage` состоит из:
   - `content` - текст, который мы получаем из сервиса `caila.io`, и который мы передадим в `YandexGPT` для обработки.
   - `role` - это поле имеет два значения в `YandexGPT`:
      1. `"system"` - ключевая тема нашего сообщения.
      2. `"user"` - сам текст.

3. `temperature` - поле, которое отвечает за настроение и эмоциональную окраску ответа `YandexGPT`. Оно содержится в формате `Double` от `0.1` до `1.0`.
4. `maxTokens` - поле, которое определяет максимальное количество токенов.
5. `stream` - поле, которое определяет, будет ли ответ подаваться построчно или в один момент. У нас значение будет `false`, т.к. мы будем передавать единоразово сообщение.

Пример JSON запроса для `YandexGPT`:

```
{
  "modelUri": "gpt://<folder_ID>/yandexgpt-lite",
  "completionOptions": {
    "stream": false,
    "temperature": 0.6,
    "maxTokens": "2000"
  },
  "messages": [
    {
      "role": "system",
      "text": "Find errors in the text and fix them"
    },
    {
      "role": "user",
      "text": "Laminate flooring is sutiable for instalation in the kitchen or in a child's room. It withsatnds moisturre and mechanical dammage thanks to a proctive layer of melamine films 0.2 mm thick and a wax-treated interlocking systme."
    }
  ]
}
```

`modelUri` — идентификатор модели, которая будет использоваться для генерации ответа. Параметр содержит идентификатор каталога Yandex Cloud или идентификатор дообученной в DataSphere модели.

`completionOptions` — параметры конфигурации запроса:

- `stream` — включает потоковую передачу частично сгенерированного текста. Принимает значения `true` или `false`.
- `temperature` — чем выше значение этого параметра, тем более креативными и случайными будут ответы модели. Принимает значения от `0` (включительно) до `1` (включительно). Значение по умолчанию: `0.6`.
- `maxTokens` — устанавливает ограничение на выход модели в токенах. Максимальное число токенов генерации зависит от модели.

`messages` — список сообщений, которые задают контекст для модели:

- `role` — роль отправителя сообщения:
   - `user` — предназначена для отправки пользовательских сообщений к модели.
   - `system` — позволяет задать контекст запроса и определить поведение модели.
   - `assistant` — используется для ответов, которые генерирует модель. При работе в режиме чата ответы модели, помеченные с ролью `assistant`, включаются в состав сообщения для сохранения контекста беседы. Не передавайте сообщения пользователя с этой ролью.
- `text` — текстовое содержимое сообщения.

Документация - [YandexGPT API](https://cloud.yandex.com/en/docs/yandexgpt/quickstart#api_1)


---

Конечно, вот исправленная версия инструкции в формате Markdown для README:

---

### 4. Соединяем IDE с сервисом Caila.io

Для того чтобы наш сервис в Caila.io имел соединение с нашей средой разработки (IDE), нам нужно добавить в наше окружение (контекст) переменные окружения, которые мы брали ранее на этапе "Создание сервиса в Caila.io".

1. В IntelliJ IDEA (или другом продукте JetBrains) сверху рядом с кнопками "Run", "Debug", "Stop" нажмите на кнопку Main.kt (название вашего класса). Если такого класса нет, то запустите проект.
2. Далее нажмите на "Edit Configurations" и в классе Main в разделе "Environment variables" вставьте полученные ранее из Caila.io переменные окружения.

   Переменные должны быть разделены знаком ';'.

   **Пример:**
   Было:
   ```
   MLP_ACCOUNT_ID=234567
   MLP_MODEL_ID=3456
   MLP_INSTANCE_ID=9898
   ```
   Стало:
   ```kotlin
   MLP_ACCOUNT_ID=234567;
   MLP_MODEL_ID=3456;
   MLP_INSTANCE_ID=9898;
   ```

3. Далее укажем в нашем приложении, что мы будем брать эти переменные (env) из окружения IDE (контекста).

   Это делается следующим образом:

```kotlin
class Main(
    override val context: MlpExecutionContext
) : MlpPredictServiceBase<ChatCompletionRequest, ChatCompletionResult>() 
```

Здесь мы присваиваем переменной `context` типа `MlpExecutionContext` значения из окружения при помощи поля `systemContext` (будет далее), то есть значения, которые мы берем из контекста нашей IDE.

---

Конечно, вот исправленная версия инструкции в формате Markdown для README:

---

### 5. Подготовка к запуску приложения

Для запуска нашего приложения мы должны создать функцию `main`, которая будет являться точкой входа в наше приложение. В этой функции мы создаем экземпляр класса `MlpServiceSDK`.

Вызываем методы:
- `start()` для запуска приложения
- `blockUntilShutdown()` для ожидания завершения приложения

```kotlin
fun main() {
    // Здесь мы передаем лямбда выражение
    val actionSDK = MlpServiceSDK({ Main(MlpExecutionContext.systemContext) })

    actionSDK.start()
    actionSDK.blockUntilShutdown()
}
```

Для того чтобы удовлетворить конструктор `MlpPredictServiceBase` в классе `Main`, мы создадим объекты `ChatCompletionRequest` и `ChatCompletionResult`.

```kotlin
class Main(
    override val context: MlpExecutionContext
) : MlpPredictServiceBase<ChatCompletionRequest, ChatCompletionResult>(REQUEST_EXAMPLE, RESPONSE_EXAMPLE) {

    companion object {
        val REQUEST_EXAMPLE = ChatCompletionRequest(
            model = null,
            messages = emptyList(),
            temperature = null,
            topP = null,
            n = null,
            stream = null,
            stop = emptyList(),
            maxTokens = null,
            presencePenalty = null,
            frequencyPenalty = null,
            logitBias = null,
            user = null
        )
        val RESPONSE_EXAMPLE = ChatCompletionResult(
            null,
            "message",
            created = System.currentTimeMillis(),
            "",
            emptyList(),
            null
        )
    }
}
```


---

6. 







5. Вэб сервис Caila формирует JSON
6. Этот JSON состоит из:

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



_______
1. Что перекладываем в объект ChatResult
2. env переменная. Где брать и куда вставлять