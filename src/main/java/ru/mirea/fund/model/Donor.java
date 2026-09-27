package ru.mirea.fund.model; // Пакет доменной модели: классы-сущности фонда.

import java.time.LocalDate; // Дата без времени: для даты регистрации часы не нужны.

// Донор (жертвователь) — одна строка таблицы donors.
// Класс только хранит данные: он ничего не знает ни про SQL, ни про меню.
// Все поля закрыты (private), снаружи доступ идёт через геттеры и сеттеры — это инкапсуляция.
public class Donor {

    private int id; // Первичный ключ; у новой записи 0, реальное значение выдаёт база.
    private String fullName; // ФИО донора, показывается в списках и в поиске.
    private String email; // Email; в таблице на него стоит UNIQUE — двух одинаковых быть не может.
    private String phone; // Контактный телефон для связи с донором.
    private String city; // Город; по нему работает фильтрация доноров.
    private LocalDate registeredAt; // Дата регистрации в фонде, задним числом не меняется.

    // Конструктор для НОВОГО донора, которого ещё нет в базе.
    // id = 0 означает «ещё не сохранён», дата регистрации — сегодняшняя.
    public Donor(String fullName, String email, String phone, String city) {
        this(0, fullName, email, phone, city, LocalDate.now()); // this(...) вызывает полный конструктор, чтобы не дублировать присваивания.
    }

    // Полный конструктор: им пользуется репозиторий, когда собирает объект из строки ResultSet.
    public Donor(int id, String fullName, String email, String phone, String city, LocalDate registeredAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.registeredAt = registeredAt;
    }

    public int getId() { // Отдаёт идентификатор: по нему сервисы ищут и обновляют донора.
        return id;
    }

    public void setId(int id) { // Проставляет id, который вернула база после INSERT.
        this.id = id;
    }

    public String getFullName() { // ФИО нужно меню для вывода и статистике для группировки.
        return fullName;
    }

    public void setFullName(String fullName) { // Меняет ФИО; корректность проверяет сервис, а не сам класс.
        this.fullName = fullName;
    }

    public String getEmail() { // Email используется при проверке уникальности перед сохранением.
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

    public String getCity() { // Город читает фильтр доноров по городу.
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

    // Переопределяем toString, чтобы донора можно было вывести через println(donor)
    // и получить читаемую строку вместо адреса объекта вида Donor@1b6d3586.
    @Override
    public String toString() {
        return String.format("#%d %s | %s | %s | %s | с %s",
                id, fullName, email, phone, city, registeredAt);
    }
}
