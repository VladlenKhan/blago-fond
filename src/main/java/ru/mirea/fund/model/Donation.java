package ru.mirea.fund.model; // Пакет доменной модели: классы-сущности фонда.

import java.math.BigDecimal; // Точный тип для денег: в отличие от double не накапливает ошибку округления.
import java.time.LocalDateTime; // Дата вместе со временем: в базе created_at имеет тип TIMESTAMP.

// Пожертвование — основная сущность системы, одна строка таблицы donations.
// Связь с донором хранится в поле donorId (внешний ключ donations.donor_id -> donors.id).
public class Donation {

    private int id; // Первичный ключ; у новой записи 0, реальное значение выдаёт база.
    private int donorId; // Ссылка на донора — именно это значение лежит в таблице donations.
    private String donorName; // Имя донора из JOIN с donors; в самой таблице его нет, но оно нужно для вывода и поиска.
    private String purpose; // Назначение сбора: на что именно идут деньги.
    private DonationCategory category; // Направление помощи; enum вместо строки защищает от опечаток.
    private DonationStatus status; // Стадия обработки; enum сам знает, какие переходы разрешены.
    private BigDecimal amount; // Сумма пожертвования: деньги считаем только BigDecimal.
    private LocalDateTime createdAt; // Момент создания записи; по нему работают поиск за период и сортировка.

    // Конструктор для нового пожертвования.
    // Статус всегда NEW: начать сразу с «Завершено» бизнес-логика не разрешает.
    public Donation(int donorId, String purpose, DonationCategory category, BigDecimal amount) {
        this(0, donorId, null, purpose, category, DonationStatus.NEW, amount, LocalDateTime.now()); // Имя донора пока неизвестно — его подставит репозиторий после чтения из базы.
    }

    // Полный конструктор: им пользуется репозиторий, когда собирает объект из строки ResultSet.
    public Donation(int id, int donorId, String donorName, String purpose, DonationCategory category,
                    DonationStatus status, BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.donorId = donorId;
        this.donorName = donorName;
        this.purpose = purpose;
        this.category = category;
        this.status = status;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public int getId() { // По id сервисы находят, изменяют и удаляют пожертвование.
        return id;
    }

    public void setId(int id) { // Проставляет id, который вернула база после INSERT.
        this.id = id;
    }

    public int getDonorId() { // Идентификатор донора уходит в SQL-запрос при сохранении.
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() { // Имя донора читают вывод таблицы и подсчёт топ-3 в статистике.
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getPurpose() { // Назначение — то поле, по которому идёт поиск через LIKE.
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public DonationCategory getCategory() { // Направление нужно фильтру и разрезу статистики по категориям.
        return category;
    }

    public void setCategory(DonationCategory category) {
        this.category = category;
    }

    public DonationStatus getStatus() { // Статус проверяется перед изменением и удалением записи.
        return status;
    }

    public void setStatus(DonationStatus status) { // Меняет статус; допустимость перехода проверяет сервис.
        this.status = status;
    }

    public BigDecimal getAmount() { // Сумму используют фильтр по диапазону, сортировка и все расчёты статистики.
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() { // Дата создания нужна сортировке по времени и поиску за период.
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Читаемая строка для вывода одной записи в консоль.
    @Override
    public String toString() {
        return String.format("#%d %s | %s | %s | %s руб. | %s | %s",
                id, purpose, category.getTitle(), status.getTitle(), amount, donorName, createdAt.toLocalDate()); // getTitle() даёт понятное название вместо кода MEDICINE или NEW.
    }
}
