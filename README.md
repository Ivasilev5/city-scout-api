# CityScout API

REST API путеводителя по городу: точки интереса (достопримечательности), отзывы и оценки пользователей.

Учебная практика — задание «Spring, вариант 2».

## Возможности

- Поиск точек интереса в заданном радиусе от пользователя с фильтрацией по категории и/или минимальной средней оценке, сортировкой (по расстоянию / рейтингу / названию) и ограничением количества результатов (по умолчанию — 10, ближайшие)
- Просмотр карточки точки интереса со средней оценкой и количеством отзывов
- Добавление точки интереса
- Оценка точки интереса (1–5)
- Написание отзыва (оценка + комментарий)
- Просмотр всех отзывов по точке интереса

## Стек

| Слой         | Технология                          |
|--------------|--------------------------------------|
| Язык / рантайм | Java 17                             |
| Фреймворк    | Spring Boot 3.3 (Web, Data JPA, Validation) |
| СУБД         | PostgreSQL 16                        |
| Миграции     | Flyway                               |
| Документация API | springdoc-openapi / Swagger UI  |
| Сборка       | Maven                                |
| Тесты        | JUnit 5, Mockito, AssertJ, Spring Boot Test |
| Контейнеризация | Docker, Docker Compose             |

DTO реализованы через Java record без Lombok/MapStruct — меньше кодогенерации, проще читать.

## Структура проекта

```
src/main/java/com/cityscout/api/
├── CityScoutApplication.java
├── config/OpenApiConfig.java
├── domain/            PointOfInterest, Review, PoiCategory
├── repository/         Spring Data JPA репозитории + проекция RatingStats
├── service/            PoiService (бизнес-логика), GeoUtils (Haversine)
├── web/                 PoiController
├── web/dto/              record'ы запросов/ответов
└── exception/           PoiNotFoundException, ApiExceptionHandler, ApiError

src/main/resources/
├── application.yml
└── db/migration/        V1__init_schema.sql, V2__seed_data.sql (Flyway)
```

## Запуск через Docker Compose (рекомендуется)

```bash
docker compose up --build
```

Поднимутся PostgreSQL и приложение. Миграции и seed-данные применяются автоматически при старте.

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Локальный запуск без Docker

Предварительно:
- Java 17+
- Maven 3.8+
- Запущенный PostgreSQL с базой `cityscout` (пользователь/пароль `cityscout`/`cityscout`), либо переопределите переменные окружения `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`

```bash
mvn spring-boot:run
```

Flyway применит миграции автоматически при старте приложения.

## Тесты

```bash
mvn test
```

Тесты не требуют внешней БД — используется встроенная H2 (в режиме совместимости с PostgreSQL). Покрыты:

- `GeoUtilsTest` — корректность расчёта расстояния по формуле гаверсинуса
- `PoiServiceTest` — бизнес-логика поиска рядом (фильтры по радиусу/категории/рейтингу, сортировка, лимит), создание точек, добавление отзывов/оценок, обработка отсутствующей точки
- `PoiControllerTest` — HTTP-слой через MockMvc: коды ответов, валидация тела запроса, сериализация
- `PointOfInterestRepositoryTest` — корректность JPA-запросов и агрегации рейтинга (`@DataJpaTest`)
- `CityScoutApplicationTests` — смоук-тест поднятия контекста Spring

## API

Базовый путь: `/api/poi`

| Метод | Путь                     | Описание                                    |
|-------|--------------------------|----------------------------------------------|
| POST  | `/api/poi`                | Создать точку интереса                       |
| GET   | `/api/poi/{id}`           | Карточка точки (со средней оценкой)          |
| GET   | `/api/poi/nearby`         | Поиск в радиусе с фильтрами и сортировкой    |
| POST  | `/api/poi/{id}/ratings`   | Выставить оценку (1–5)                        |
| POST  | `/api/poi/{id}/reviews`   | Написать отзыв (оценка + комментарий)         |
| GET   | `/api/poi/{id}/reviews`   | Список отзывов по точке                       |

### `GET /api/poi/nearby` — параметры

| Параметр   | Тип    | Обязателен | По умолчанию | Описание                                  |
|------------|--------|:----------:|:------------:|--------------------------------------------|
| `lat`      | double |     да     | —            | Широта пользователя                        |
| `lon`      | double |     да     | —            | Долгота пользователя                       |
| `radiusKm` | double |     да     | —            | Радиус поиска, км                          |
| `category` | string |     нет    | —            | `MUSEUM`, `PARK`, `LANDMARK`, `TEMPLE`, `THEATRE`, `RESTAURANT`, `SHOP`, `OTHER` |
| `minRating`| double |     нет    | —            | Минимальная средняя оценка (1–5)           |
| `limit`    | int    |     нет    | 10           | Максимум результатов (не больше 100)       |
| `sortBy`   | string |     нет    | `DISTANCE`   | `DISTANCE` / `RATING` / `NAME`             |

## Примеры запросов

```bash
# Создать точку интереса
curl -X POST http://localhost:8080/api/poi \
  -H "Content-Type: application/json" \
  -d '{"name":"Казанский Кремль","category":"LANDMARK","latitude":55.7989,"longitude":49.1064,"address":"ул. Кремлёвская, 2"}'

# Найти точки в радиусе 3 км, отсортированные по рейтингу, с минимальной оценкой 4
curl "http://localhost:8080/api/poi/nearby?lat=55.7989&lon=49.1064&radiusKm=3&minRating=4&sortBy=RATING"

# Выставить оценку
curl -X POST http://localhost:8080/api/poi/1/ratings \
  -H "Content-Type: application/json" \
  -d '{"reviewerName":"Иван","rating":5}'

# Написать отзыв
curl -X POST http://localhost:8080/api/poi/1/reviews \
  -H "Content-Type: application/json" \
  -d '{"reviewerName":"Мария","rating":5,"comment":"Потрясающее место!"}'

# Получить отзывы по точке
curl http://localhost:8080/api/poi/1/reviews
```

## Модель данных

**points_of_interest** — id, name, category, latitude, longitude, address, created_at

**reviews** — id, poi_id (FK → points_of_interest, ON DELETE CASCADE), reviewer_name, rating (1–5, CHECK), comment (nullable), created_at

Средняя оценка и количество отзывов вычисляются агрегирующим запросом `AVG/COUNT ... GROUP BY poi_id`, а не хранятся денормализованно — исключает рассинхронизацию при добавлении/удалении отзывов.

## Категории точек интереса

`MUSEUM`, `PARK`, `LANDMARK`, `TEMPLE`, `THEATRE`, `RESTAURANT`, `SHOP`, `OTHER`
