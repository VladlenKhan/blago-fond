package ru.mirea.fund.model; // Объявляем пакет класса.

import java.time.LocalDate; // Подключаем необходимый тип.

/** Донор (жертвователь) — участник предметной области, строка таблицы donors. */
public class Donor { // Модель донора: хранит данные, не знает про БД и меню.

    private int id; // Первичный ключ, выдаёт база данных.
    private String fullName; // ФИО донора.
    private String email; // Email, уникален в таблице donors.
    private String phone; // Контактный телефон.
    private String city; // Город донора.
    private LocalDate registeredAt; // Дата регистрации в фонде.

    public Donor(String fullName, String email, String phone, String city) { // Конструктор нового донора: id выдаст база.
        this(0, fullName, email, phone, city, LocalDate.now()); // Вызываем полный конструктор.
    } // Завершаем блок.

    public Donor(int id, String fullName, String email, String phone, String city, LocalDate registeredAt) { // Полный конструктор: собирает объект из строки БД.
        this.id = id; // Сохраняем значение в поле.
        this.fullName = fullName; // Сохраняем значение в поле.
        this.email = email; // Сохраняем значение в поле.
        this.phone = phone; // Сохраняем значение в поле.
        this.city = city; // Сохраняем значение в поле.
        this.registeredAt = registeredAt; // Сохраняем значение в поле.
    } // Завершаем блок.

    public int getId() { // Возвращает идентификатор донора.
        return id; // Возвращаем результат.
    } // Завершаем блок.

    public void setId(int id) { // Задаёт идентификатор после сохранения в БД.
        this.id = id; // Сохраняем значение в поле.
    } // Завершаем блок.

    public String getFullName() { // Возвращает ФИО донора.
        return fullName; // Возвращаем результат.
    } // Завершаем блок.

    public void setFullName(String fullName) { // Задаёт новое ФИО донора.
        this.fullName = fullName; // Сохраняем значение в поле.
    } // Завершаем блок.

    public String getEmail() { // Возвращает email донора.
        return email; // Возвращаем результат.
    } // Завершаем блок.

    public void setEmail(String email) { // Задаёт новый email донора.
        this.email = email; // Сохраняем значение в поле.
    } // Завершаем блок.

    public String getPhone() { // Возвращает телефон донора.
        return phone; // Возвращаем результат.
    } // Завершаем блок.

    public void setPhone(String phone) { // Задаёт новый телефон донора.
        this.phone = phone; // Сохраняем значение в поле.
    } // Завершаем блок.

    public String getCity() { // Возвращает город донора.
        return city; // Возвращаем результат.
    } // Завершаем блок.

    public void setCity(String city) { // Задаёт новый город донора.
        this.city = city; // Сохраняем значение в поле.
    } // Завершаем блок.

    public LocalDate getRegisteredAt() { // Возвращает дату регистрации донора.
        return registeredAt; // Возвращаем результат.
    } // Завершаем блок.

    public void setRegisteredAt(LocalDate registeredAt) { // Задаёт дату регистрации донора.
        this.registeredAt = registeredAt; // Сохраняем значение в поле.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public String toString() { // Собирает читаемую строку донора для вывода.
        return String.format("#%d %s | %s | %s | %s | с %s", // Возвращаем результат.
                id, fullName, email, phone, city, registeredAt); // Подставляем значения полей.
    } // Завершаем блок.
} // Завершаем блок.
