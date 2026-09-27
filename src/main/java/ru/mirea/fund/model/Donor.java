package ru.mirea.fund.model;

import java.time.LocalDate;

/** Донор (жертвователь) — участник предметной области. Все поля private (инкапсуляция). */
public class Donor {

    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String city;
    private LocalDate registeredAt;

    /** Конструктор для новой записи (ID выдаёт база данных). */
    public Donor(String fullName, String email, String phone, String city) {
        this(0, fullName, email, phone, city, LocalDate.now());
    }

    /** Полный конструктор — используется при чтении из базы данных. */
    public Donor(int id, String fullName, String email, String phone, String city, LocalDate registeredAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.registeredAt = registeredAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDate registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return String.format("#%d %s | %s | %s | %s | с %s",
                id, fullName, email, phone, city, registeredAt);
    }
}
