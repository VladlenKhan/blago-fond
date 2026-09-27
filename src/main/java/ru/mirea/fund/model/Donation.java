package ru.mirea.fund.model; // Объявляем пакет класса.

import java.math.BigDecimal; // Подключаем необходимый тип.
import java.time.LocalDateTime; // Подключаем необходимый тип.

/** Пожертвование — основная сущность системы, строка таблицы donations. */
public class Donation { // Модель пожертвования, связана с донором через donorId.

    private int id; // Первичный ключ, выдаёт база данных.
    private int donorId; // Внешний ключ на таблицу donors.
    private String donorName; // Имя донора из JOIN, нужно для вывода и поиска.
    private String purpose; // Назначение: на что собираются деньги.
    private DonationCategory category; // Направление помощи вместо «магической строки».
    private DonationStatus status; // Стадия обработки пожертвования.
    private BigDecimal amount; // Сумма деньгами: BigDecimal точнее double.
    private LocalDateTime createdAt; // Дата и время создания записи.

    public Donation(int donorId, String purpose, DonationCategory category, BigDecimal amount) { // Конструктор нового пожертвования: статус всегда NEW.
        this(0, donorId, null, purpose, category, DonationStatus.NEW, amount, LocalDateTime.now()); // Вызываем полный конструктор.
    } // Завершаем блок.

    public Donation(int id, int donorId, String donorName, String purpose, DonationCategory category, // Полный конструктор: собирает объект из строки БД.
                    DonationStatus status, BigDecimal amount, LocalDateTime createdAt) { // Перечисляем параметры.
        this.id = id; // Сохраняем значение в поле.
        this.donorId = donorId; // Сохраняем значение в поле.
        this.donorName = donorName; // Сохраняем значение в поле.
        this.purpose = purpose; // Сохраняем значение в поле.
        this.category = category; // Сохраняем значение в поле.
        this.status = status; // Сохраняем значение в поле.
        this.amount = amount; // Сохраняем значение в поле.
        this.createdAt = createdAt; // Сохраняем значение в поле.
    } // Завершаем блок.

    public int getId() { // Возвращает идентификатор пожертвования.
        return id; // Возвращаем результат.
    } // Завершаем блок.

    public void setId(int id) { // Задаёт идентификатор после сохранения в БД.
        this.id = id; // Сохраняем значение в поле.
    } // Завершаем блок.

    public int getDonorId() { // Возвращает идентификатор донора.
        return donorId; // Возвращаем результат.
    } // Завершаем блок.

    public void setDonorId(int donorId) { // Задаёт донора пожертвования.
        this.donorId = donorId; // Сохраняем значение в поле.
    } // Завершаем блок.

    public String getDonorName() { // Возвращает имя донора для вывода.
        return donorName; // Возвращаем результат.
    } // Завершаем блок.

    public void setDonorName(String donorName) { // Задаёт имя донора, полученное из JOIN.
        this.donorName = donorName; // Сохраняем значение в поле.
    } // Завершаем блок.

    public String getPurpose() { // Возвращает назначение пожертвования.
        return purpose; // Возвращаем результат.
    } // Завершаем блок.

    public void setPurpose(String purpose) { // Задаёт новое назначение пожертвования.
        this.purpose = purpose; // Сохраняем значение в поле.
    } // Завершаем блок.

    public DonationCategory getCategory() { // Возвращает направление помощи.
        return category; // Возвращаем результат.
    } // Завершаем блок.

    public void setCategory(DonationCategory category) { // Задаёт новое направление помощи.
        this.category = category; // Сохраняем значение в поле.
    } // Завершаем блок.

    public DonationStatus getStatus() { // Возвращает текущий статус пожертвования.
        return status; // Возвращаем результат.
    } // Завершаем блок.

    public void setStatus(DonationStatus status) { // Задаёт новый статус пожертвования.
        this.status = status; // Сохраняем значение в поле.
    } // Завершаем блок.

    public BigDecimal getAmount() { // Возвращает сумму пожертвования.
        return amount; // Возвращаем результат.
    } // Завершаем блок.

    public void setAmount(BigDecimal amount) { // Задаёт новую сумму пожертвования.
        this.amount = amount; // Сохраняем значение в поле.
    } // Завершаем блок.

    public LocalDateTime getCreatedAt() { // Возвращает дату создания записи.
        return createdAt; // Возвращаем результат.
    } // Завершаем блок.

    public void setCreatedAt(LocalDateTime createdAt) { // Задаёт дату создания записи.
        this.createdAt = createdAt; // Сохраняем значение в поле.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public String toString() { // Собирает читаемую строку пожертвования для вывода.
        return String.format("#%d %s | %s | %s | %s руб. | %s | %s", // Возвращаем результат.
                id, purpose, category.getTitle(), status.getTitle(), amount, donorName, createdAt.toLocalDate()); // Подставляем значения полей.
    } // Завершаем блок.
} // Завершаем блок.
