# MLP Java SDK



## Запуск приложения при помощи SDK

### 1. Содержание функции main

```kotlin
fun main() {
    // Создается экземпляр MlpServiceSDK с переданным в конструкторе экземпляром ML сервиса
    val actionSDK = MlpServiceSDK({ YourMlpService(MlpExecutionContext.systemContext) })

    // SDK запускается при помощи метода start
    actionSDK.start()

    // Метод blockUntilShutdown() ожидает, пока сервис, запущенный с помощью actionSDK.start(), не завершится или не будет остановлен
    // Это обычно используется для того, чтобы гарантировать, что приложение не завершится до того, как сервис завершит свою работу или не будет остановлен вручную
    actionSDK.blockUntilShutdown()
}
```

### 2. Для работы с MlpServiceSDK необходимы аргументы
- Объект класса MlpService
- Опционально MlpServiceConfig

### 3. Реализация MlpService
Чтобы реализовать MlpService, необходимо создать его подкласс. В приведенном ниже примере показано, как это можно сделать:

```kotlin
class YourMlpService : MlpService() {
    // Здесь мы наследуемся от MlpService
    // Вы можете переопределить методы, которые вы хотите, чтобы они использовались в вашем MlpService
}
```

### 4. Инициализация контекста окружения

Также в подклассе мы определяем контекст окружения (параметры нашей среды разработки). Делается это через создание контекста внутри класса, который наследуется от MlpService
```
override val context: MlpExecutionContext = MlpExecutionContext.systemContext // Пример инициализации контекста
```
Здесь мы инициализируем контекст в виде объекта класса ```MlpExecutionContext```. Этот объект инициализируется с помощью systemContext, который, предварительно настроен и содержит системные параметры и конфигурации, необходимые для выполнения операций в рамках вашего приложения.

Например, переменные ```MLP_ACCOUNT_ID, MLP_MODEL_ID, MLP_INSTANCE_ID```, и другие, являются переменными окружения, которые предоставляют информацию о нашем аккаунте, модели, идентификаторе экземпляра и т. д.
Приложение может использовать эти параметры как часть контекста выполнения для получения доступа к этой информации в различных частях кода без явного указания этих значений в коде. Таким образом, ```MlpExecutionContext``` может быть инициализирован с использованием этих переменных окружения, предоставляя доступ к общим данным приложения в удобной и единообразной форме.


## Документация SDK

Для более детального ознакомления Вы можете перейти по описанию

1. **Connector**
    - [Описание коннектора](docs/Connector.md)

2. **ConnectorPool**
    - [Описание пула коннекторов](docs/ConnectorPool.md)

3. **MlpApiClient**
    - [Описание клиента MLP API](docs/MlpApiClient.md)

4. **MlpClientConfig**
    - [Описание конфигурации клиента MLP](docs/MlpClientConfig.md)

5. **MlpClientException**
    - [Описание исключений клиента MLP](docs/MlpClientException.md)

6. **MlpClientHelper**
    - [Описание вспомогательного класса клиента MLP](docs/MlpClientHelper.md)

7. **MlpClientSDK**
    - [Описание SDK клиента MLP](docs/MlpClientSDK.md)

8. **MlpExecutionContext**
    - [Описание контекста выполнения MLP](docs/MlpExecutionContext.md)

9. **MlpService**
    - [Описание сервиса MLP](docs/MlpService.md)

10. **MlpServiceBase**
    - [Описание базового класса сервиса MLP](docs/MlpServiceBase.md)

11. **MlpServiceConfig**
    - [Описание конфигурации сервиса MLP](docs/MlpServiceConfig.md)

12. **MlpServiceSDK**
    - [Описание SDK сервиса MLP](docs/MlpServiceSDK.md)

13. **MlpService vs MlpServiceSDK**
    - [Сравнение сервиса MLP и его SDK](docs/MlpService%20vs%20MlpServiceSDK.md)

14. **Payload**
    - [Описание полезной нагрузки](docs/Payload.md)

15. **State**
    - [Описание состояния](docs/State.md)

16. **TaskExecutor**
    - [Описание исполнителя задач](docs/TaskExecutor.md)

17. **TimeTracker**
    - [Описание трекера времени](docs/TimeTracker.md)

## Utils

1. **ConfigHelper**
    - [Описание помощника конфигурации](docs/utils/ConfigHelper.md)

2. **JSON**
    - [Описание работы с JSON](docs/utils/JSON.md)

3. **JobsContainer**
    - [Описание контейнера задач](docs/utils/JobsContainer.md)

## Storage

1. **LocalStorage**
    - [Описание локального хранилища](docs/storage/LocalStorage.md)

2. **S3Storage**
    - [Описание хранилища S3](docs/storage/S3Storage.md)

3. **Storage**
    - [Описание хранилища](docs/storage/Storage.md)

4. **StorageFactory**
    - [Описание фабрики хранилищ](docs/storage/StorageFactory.md)

## Data Types

### AIProxy

1. **AIProxy**
    - [Описание типов данных и запросов для работы с AIProxy](docs/datatypes/aiproxy/AIProxy.md)

### ASR (Automatic Speech Recognition)

1. **Kaldi**
    - [Описание типов данных и запросов для распознавания речи](docs/datatypes/asr/Kaldi.md)

### Chat

1. **Chat**
    - [Описание типов данных и запросов для работы с чатами](docs/datatypes/chat/Chat.md)

### ChatGPT

1. **GtpLike**
    - [Описание типов данных и запросов для работы с ChatGPT](docs/datatypes/chatgtp/GtpLike.md)

### Datasets

1. **TextsAndLabels**
    - [Описание типов данных для хранения текстов и меток](docs/datatypes/datasets/TextsAndLabels.md)

### JAICP Patterns

1. **PatternData**
    - [Описание типов данных и запросов для работы с шаблонами JAICP](docs/datatypes/jaicp_patterns/PatternData.md)

### Llama

1. **Llama**
    - [Описание типов данных и запросов для работы с Llama](docs/datatypes/llama/Llama.md)

### TaskZoo

1. **TaskZoo**
    - [Описание типов данных и запросов для работы с TaskZoo](docs/datatypes/taskzoo/TaskZoo.md)

### TTS (Text-to-Speech)

1. **Aimvoice**
    - [Описание типов данных и запросов для синтеза речи из текста](docs/datatypes/tts/Aimvoice.md)