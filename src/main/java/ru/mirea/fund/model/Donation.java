package ru.mirea.fund.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Пожертвование — основная сущность системы. Связано с донором через donorId. */
public class Donation {

    private int id;
    private int donorId;
    private String donorName;          // имя донора из связанной таблицы (для вывода и поиска)
    private String purpose;            // назначение пожертвования
    private DonationCategory category;
    private DonationStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    /** Конструктор для новой записи: статус всегда NEW, дата — текущая. */
    public Donation(int donorId, String purpose, DonationCategory category, BigDecimal amount) {
        this(0, donorId, null, purpose, category, DonationStatus.NEW, amount, LocalDateTime.now());
    }

    /** Полный конструктор — используется при чтении из базы данных. */
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public DonationCategory getCategory() {
        return category;
    }

    public void setCategory(DonationCategory category) {
        this.category = category;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("#%d %s | %s | %s | %s руб. | %s | %s",
                id, purpose, category.getTitle(), status.getTitle(), amount, donorName, createdAt.toLocalDate());
    }
}
