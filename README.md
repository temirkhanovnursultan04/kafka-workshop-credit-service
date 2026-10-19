# kafka-workshop-credit-service

Учебный проект с воркшопа **Coding GYM / Java — «Асинхронщина: Kafka — трогаем руками»** (Kata Academy).

Работает в паре с [kafka-workshop-notification-service](../kafka-workshop-notification-service).

## О проекте

**credit-service** — сервис, который принимает заявку на кредит и решает, одобрить её или нет:

- сумма к возврату = сумма кредита + 20%;
- ежемесячный платёж = сумма к возврату / срок в месяцах (округление вверх до копеек);
- кредит одобрен, если платёж не больше половины зарплаты.

Решение сервис отправляет в **notification-service**, который пишет уведомление в Telegram-канал. Отправить можно двумя способами, и в этом весь смысл проекта:

| Эндпоинт | Как общаются сервисы | Что видит клиент |
|---|---|---|
| `POST /api/credits/sync` | синхронно, REST (`RestTemplate`) | ждёт ответа notification-service (~3 с), `200 OK` |
| `POST /api/credits/async` | асинхронно, Kafka (`KafkaTemplate`) | ответ сразу, `202 Accepted` |

```
/sync : Postman → credit-service ──REST──▶ notification-service → Telegram
/async: Postman → credit-service ──▶ Kafka topic ◀── notification-service → Telegram
```

credit-service — **producer**. Топик `kafka.workflow.topic` (3 партиции, replication factor 3, `min.insync.replicas` 2) он создаёт сам при старте (`KafkaConfig`, бин `NewTopic`). Ключ сообщения — фамилия клиента.

**Стек:** Java 25, Spring Boot 4, Spring Kafka, Apache Kafka 4.1.1 (KRaft, 3 брокера в Docker), Lombok.

## Структура

```
client/      CreditRestClient, CreditKafkaClient — отправка решения по REST и в Kafka
config/      KafkaConfig (producer + топик), RestTemplateConfig
controller/  CreditApiController — /sync и /async
dto/         CreditRequestDto, CreditResponseDto
service/     CreditSyncService, CreditAsyncService — расчёт кредита
docker-compose.yml — кластер Kafka из 3 брокеров
```

## Как работать с Kafka: пошаговый гайд

### 1. Docker и docker-compose

1. Установите [Docker Desktop](https://www.docker.com/products/docker-desktop/).
2. Откройте `docker-compose.yml` и разберитесь, что в нём происходит: 3 узла `apache/kafka:4.1.1`, каждый одновременно controller и broker (KRaft, без ZooKeeper), порты 9092 / 9094 / 9096. Комментарии к настройкам — в файле.
3. Создайте `.env` из примера и запустите кластер:

Остановить: `docker compose down`. Данные брокеров лежат в `./volumes` — удалите папку, чтобы начать с чистого кластера.

### 2. Запуск сервисов

1. Запустите **credit-service** (порт 8080) — он создаст топик.
2. Запустите **notification-service** (порт 8081).

### 3. Postman: проверяем запросы

Одобрят:

```json
POST http://localhost:8080/api/credits/sync
{"name":"Иван","surname":"Иванов","creditSum":1000000,"creditPeriodInMonths":24,"salary":150000}
```

Откажут:

```json
POST http://localhost:8080/api/credits/async
{"name":"Анатолий","surname":"Сидоров","creditSum":3000000,"creditPeriodInMonths":12,"salary":300000}
```

Сравните время ответа `/sync` и `/async`.

### 4. Плагин Kafka в IntelliJ IDEA

1. `Settings → Plugins → Marketplace` → установите плагин **Kafka/Confluent** (JetBrains).
2. Откройте вкладку Kafka → добавьте подключение, `Bootstrap servers`: `127.0.0.1:9092`.
3. Посмотрите топик `kafka.workflow.topic`: партиции, сообщения, key, headers, offset, consumer groups.

### 5. Потренируйтесь

- Отправьте несколько заявок с одной фамилией — убедитесь, что все они в одной партиции.
- Выключите notification-service: `/sync` падает, а `/async` работает. Включите обратно — сообщения дочитаются.
- Запустите второй экземпляр notification-service (другой `SERVER_PORT`) и посмотрите, как перераспределятся партиции.
- Остановите один брокер (`docker compose stop kafka-2`) — продолжает ли всё работать?

### 6. Улучшайте

- Разберитесь с настройками producer в `application.yaml`: `acks`, `linger.ms`, `batch-size`, идемпотентность, таймауты.
- Запустите своего Telegram-бота — инструкция в README notification-service.
- Добавьте БД и миграции (Liquibase / Flyway), Swagger, валидацию запросов.
- Улучшите архитектуру и бизнес-логику, добавьте ещё один микросервис.
- Попробуйте RabbitMQ вместо Kafka и сравните.

## Переменные окружения

| Переменная | По умолчанию |
|---|---|
| `KAFKA_BOOTSTRAP_SERVER` | `localhost:9092,localhost:9094,localhost:9096` |
| `SERVER_PORT` | `8080` |
| `NOTIFICATION_URL` | `http://localhost:8081` |
| `KAFKA_TOPIC_CREDIT` | `kafka.workflow.topic` |
