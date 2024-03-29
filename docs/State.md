### Описание классов WithState и State

#### Класс `WithState`

Предоставляет возможность управления состоянием объекта, реализующего интерфейс `WithState`.

#### Поля класса `WithState`

- **state**: Объект класса `State`, представляющий текущее состояние объекта.

#### Методы класса `WithState`

- *Конструктор*:
    - `WithState(condition: Condition = NOT_STARTED)`: Создает объект `WithState` с указанным начальным состоянием.

#### Класс `State`

Представляет состояние объекта и позволяет управлять его жизненным циклом.

#### Поля класса `State`

- **condition**: Текущее состояние объекта.
- **shutdownReason**: Причина завершения работы объекта.
- **shutdownLatch**: `CountDownLatch`, используемый для ожидания завершения работы объекта.

#### Методы класса `State`

- *Методы управления состоянием*:
    - `starting()`: Переводит объект в состояние "запускается".
    - `active()`: Переводит объект в состояние "активен".
    - `shuttingDown()`: Переводит объект в состояние "завершается".
    - `shutdown()`: Переводит объект в состояние "завершен".
    - `awaitShutdown()`: Ожидает завершения работы объекта.

- *Дополнительные методы*:
    - `isShutdownTypeState()`: Проверяет, является ли текущее состояние объекта "завершающимся" или "завершенным".

#### Перечисление `State.Condition`

Представляет возможные состояния объекта.

- **NOT_STARTED**: Не запущен.
- **STARTING**: Запускается.
- **ACTIVE**: Активен.
- **SHUTTING_DOWN**: Завершается.
- **SHUT_DOWN**: Завершен.

### Использование

Пример использования классов `WithState` и `State`:

```kotlin
// Создание объекта с указанием начального состояния
val myComponent = MyComponent()
val withState = WithState(Condition.NOT_STARTED)

// Запуск компонента
withState.state.starting()

// Ожидание завершения работы компонента
withState.state.awaitShutdown()
```


