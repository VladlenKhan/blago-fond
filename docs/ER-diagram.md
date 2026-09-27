# ER-диаграмма базы данных «Благотворительный фонд»

## Схема

```mermaid
erDiagram
    DONORS ||--o{ DONATIONS : "делает"

    DONORS {
        SERIAL       id PK
        VARCHAR(100) full_name  "NOT NULL"
        VARCHAR(100) email      "NOT NULL, UNIQUE"
        VARCHAR(20)  phone      "NOT NULL"
        VARCHAR(50)  city       "NOT NULL"
        DATE         registered_at "NOT NULL, DEFAULT CURRENT_DATE"
    }

    DONATIONS {
        SERIAL        id PK
        INTEGER       donor_id FK "NOT NULL, REFERENCES donors(id) ON DELETE CASCADE"
        VARCHAR(150)  purpose    "NOT NULL"
        VARCHAR(20)   category   "NOT NULL, CHECK (5 значений)"
        VARCHAR(20)   status     "NOT NULL, CHECK (4 значения)"
        NUMERIC(12,2) amount     "NOT NULL, CHECK (0 < amount <= 1000000)"
        TIMESTAMP     created_at "NOT NULL, DEFAULT now()"
    }
```

## Текстовая схема

```
+---------------------------+              +--------------------------------+
|          donors           |              |           donations            |
+---------------------------+              +--------------------------------+
| PK id           SERIAL    |1            *| PK id            SERIAL        |
|    full_name    NOT NULL  |--------------| FK donor_id      NOT NULL      |
|    email        UNIQUE    |              |    purpose       NOT NULL      |
|    phone        NOT NULL  |              |    category      CHECK         |
|    city         NOT NULL  |              |    status        CHECK         |
|    registered_at NOT NULL |              |    amount        CHECK > 0     |
+---------------------------+              |    created_at    NOT NULL      |
                                           +--------------------------------+
```

## Связь

`donations.donor_id -> donors.id`

Связь «один ко многим»: один донор может сделать много пожертвований,
каждое пожертвование принадлежит ровно одному донору.
При удалении донора его пожертвования удаляются каскадно (`ON DELETE CASCADE`).

## Ограничения целостности

| Ограничение | Где применено | Назначение |
|---|---|---|
| `PRIMARY KEY` | `donors.id`, `donations.id` | уникальный идентификатор записи |
| `FOREIGN KEY` | `donations.donor_id` | пожертвование нельзя создать без существующего донора |
| `NOT NULL` | все обязательные поля | защита от пустых данных |
| `UNIQUE` | `donors.email` | один email — один донор |
| `CHECK` | `donations.status` | только значения enum `DonationStatus` |
| `CHECK` | `donations.category` | только значения enum `DonationCategory` |
| `CHECK` | `donations.amount` | сумма строго больше 0 и не более 1 000 000 |

## Соответствие таблиц и классов

| Таблица | Класс модели | Репозиторий | Сервис |
|---|---|---|---|
| `donors` | `Donor` | `DonorRepository` | `DonorService` |
| `donations` | `Donation` | `DonationRepository` | `DonationService` |
| столбец `status` | enum `DonationStatus` | — | — |
| столбец `category` | enum `DonationCategory` | — | — |
