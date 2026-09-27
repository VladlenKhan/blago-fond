# Благотворительный фонд — консольная информационная система

Учебный проект: Java + PostgreSQL + JDBC, многослойная архитектура.

Система учитывает **доноров** (жертвователей) и их **пожертвования**:
создание, просмотр, изменение, удаление, поиск, фильтрация, сортировка,
статистика и экспорт данных в Excel/CSV.

---

## 1. Требования

| Компонент | Версия |
|---|---|
| JDK | 17 или выше |
| Maven | 3.8+ |
| PostgreSQL | 13 или выше |

Зависимости (ставятся Maven автоматически): драйвер `postgresql`, библиотека `Apache POI` для Excel.

---

## 2. Подготовка базы данных

Создать базу данных (один раз):

```bash
psql -U postgres -f database/create_database.sql
```

или вручную в psql / pgAdmin:

```sql
CREATE DATABASE charity_fund
    TEMPLATE template0
    ENCODING 'UTF8'
    LC_COLLATE 'ru_RU.UTF-8'
    LC_CTYPE 'ru_RU.UTF-8';
```

> Локаль `ru_RU.UTF-8` важна: с локалью `C` функция `LOWER()` не понимает
> кириллицу и поиск по русским словам без учёта регистра работать не будет.

**Таблицы создавать вручную не нужно** — при первом запуске программа сама
выполнит скрипт `schema.sql`: создаст таблицы `donors`, `donations`
и загрузит тестовые данные (6 доноров, 12 пожертвований).

Если нужно создать таблицы вручную:

```bash
psql -U postgres -d charity_fund -f database/schema.sql
```

---

## 3. Настройка подключения

Файл `src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/charity_fund
db.user=postgres
db.password=12345
```

Укажите здесь свой пароль пользователя PostgreSQL.

---

## 4. Сборка и запуск

```bash
mvn package
java -jar target/blago-fond-1.0.jar
```

Запуск без сборки jar:

```bash
mvn compile exec:java -Dexec.mainClass=ru.mirea.fund.Main
```

Либо запустить класс `ru.mirea.fund.Main` из IntelliJ IDEA.

---

## 5. Главное меню

```
========================================
         БЛАГОТВОРИТЕЛЬНЫЙ ФОНД
========================================
1. Доноры
2. Пожертвования
3. Поиск
4. Фильтрация и сортировка
5. Статистика
6. Экспорт данных
7. Вывести таблицы базы данных
0. Выход
```

- **Доноры** — CRUD по донорам + список по алфавиту.
- **Пожертвования** — CRUD по пожертвованиям + смена статуса.
- **Поиск** — по назначению, по имени донора, по периоду дат, по донорам.
- **Фильтрация и сортировка** — по статусу, направлению, диапазону сумм, донору, городу;
  сортировка по сумме, по дате, по имени донора.
- **Статистика** — 6 числовых показателей + разрезы по статусам, направлениям и топ-3 донора.
- **Экспорт данных** — `export_fund.xlsx` или `export_fund.csv` в папке проекта.
- **Вывести таблицы базы данных** — структура таблиц и количество строк (через `DatabaseMetaData`).

---

## 6. Структура проекта

```
src/main/java/ru/mirea/fund/
├── Main.java                      — точка входа
├── model/                         — предметная модель
│   ├── Donor.java                 — донор
│   ├── Donation.java              — пожертвование (основная сущность)
│   ├── DonationStatus.java        — enum: статусы + правила переходов
│   └── DonationCategory.java      — enum: направления помощи
├── repository/                    — работа с БД (только SQL)
│   ├── CrudRepository.java        — общий интерфейс
│   ├── DonorRepository.java
│   └── DonationRepository.java
├── service/                       — бизнес-логика и проверки
│   ├── DonorService.java
│   ├── DonationService.java
│   └── StatisticsService.java
├── exception/                     — собственные исключения
│   ├── BusinessException.java
│   ├── EntityNotFoundException.java
│   └── DataAccessException.java
├── util/
│   ├── DatabaseManager.java       — подключение JDBC, инициализация схемы
│   ├── Exporter.java              — интерфейс экспорта
│   ├── ExcelExporter.java         — реализация .xlsx (Apache POI)
│   └── CsvExporter.java           — реализация .csv
└── ui/
    ├── ConsoleApp.java            — меню (без SQL)
    └── ConsoleInput.java          — безопасный ввод с консоли

src/main/resources/
├── db.properties                  — настройки подключения
└── schema.sql                     — таблицы + тестовые данные

database/                          — SQL-скрипты для защиты
docs/ER-diagram.md                 — ER-диаграмма
```

Слои: `ConsoleApp` → `Service` → `Repository (JDBC)` → `PostgreSQL`.
SQL-запросов в консольном меню нет.

---

## 7. Бизнес-правила

1. **Назначение пожертвования обязательно** — минимум 3 символа (`DonationService.validate`).
2. **Сумма строго больше нуля и не более 1 000 000 руб.** (`DonationService.validate`).
3. **Донор должен существовать** — перед созданием пожертвования проверяется его ID
   (`DonationService.create`), на уровне БД дублируется внешним ключом.
4. **Разрешены только переходы `NEW → CONFIRMED → COMPLETED`**, отмена возможна
   до завершения (`DonationStatus.canChangeTo`).
5. **Завершённое пожертвование нельзя изменить или удалить** — оно входит
   в отчётность фонда (`DonationService.update/delete`).
6. **Email донора уникален и должен быть корректным** (`DonorService.validate` + `UNIQUE` в БД).
7. **ФИО и город донора обязательны**, ФИО — минимум 3 символа.

---

## 8. Обработка ошибок

| Ситуация | Реакция программы |
|---|---|
| Введён текст вместо числа | `Ошибка: нужно ввести целое число.` и повтор запроса |
| Некорректная сумма или дата | сообщение с примером формата и повтор запроса |
| Нет записи с таким ID | `EntityNotFoundException` → `Ошибка: Пожертвование с ID 99 не найдено` |
| Нарушено бизнес-правило | `BusinessException` → понятное сообщение |
| Ошибка подключения к БД / SQL | `DataAccessException` → `Ошибка базы данных: ...` |

Программа никогда не завершается аварийно — после любой ошибки возвращается в меню.

---

## 9. Что показать на защите

| Требование | Где в коде |
|---|---|
| Инкапсуляция | `model/Donor.java`, `model/Donation.java` — private-поля + геттеры/сеттеры |
| Интерфейсы | `repository/CrudRepository.java`, `util/Exporter.java` |
| Полиморфизм | `ExcelExporter` и `CsvExporter` вызываются через тип `Exporter` в `ConsoleApp.exportMenu()` |
| Enum | `DonationStatus` (с методом `canChangeTo`), `DonationCategory` |
| Коллекции | `List`, `Map`, `LinkedHashMap`, `Optional` в сервисах и репозиториях |
| Stream API | `DonationService` (фильтры, сортировки), `StatisticsService` |
| Свои исключения | пакет `exception/` |
| JDBC | `Connection`, `PreparedStatement`, `ResultSet`, try-with-resources во всех репозиториях |
| PreparedStatement | параметризованные запросы, в том числе `LIKE ?` в поиске |
| Связи таблиц | `donations.donor_id → donors.id`, `ON DELETE CASCADE` |
| Экспорт | `util/ExcelExporter.java`, файл `export_fund.xlsx` |

---

## 10. Файлы для сдачи

- исходный код — `src/`
- `pom.xml`
- SQL-скрипты — `database/create_database.sql`, `database/schema.sql`
- ER-диаграмма — `docs/ER-diagram.md`
- экспортированный Excel — `export_fund.xlsx`
- инструкция по запуску — этот файл
