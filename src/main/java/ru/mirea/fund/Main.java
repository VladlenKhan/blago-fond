package ru.mirea.fund; // Корневой пакет проекта: здесь лежит только точка входа.

import ru.mirea.fund.exception.DataAccessException; // Ошибка базы: ловим её, чтобы дать понятную подсказку вместо стектрейса.
import ru.mirea.fund.ui.ConsoleApp; // Консольное меню: ему передаётся управление после проверки базы.
import ru.mirea.fund.util.DatabaseManager; // Подключение к базе и создание таблиц при первом запуске.

// Точка входа в программу.
// Здесь намеренно почти нет кода: Main только запускает приложение, а вся логика
// лежит в отдельных классах — так требует задание, запрещающее писать всё в Main.
public class Main {

    public static void main(String[] args) {
        // Сначала убеждаемся, что база доступна: если упадём здесь, показывать меню
        // бессмысленно — всё равно ни одна операция не сработает.
        try {
            DatabaseManager.checkConnection();
            DatabaseManager.initSchemaIfNeeded(); // При первом запуске создаст таблицы и зальёт тестовые данные.
        } catch (DataAccessException e) {
            // Ловим только свою ошибку БД, чтобы показать человеку понятную подсказку.
            System.out.println("Ошибка: " + e.getMessage());
            System.out.println("Проверьте, что PostgreSQL запущен и настройки в db.properties корректны.");
            return; // Выходим из программы, меню не запускаем.
        }

        new ConsoleApp().run(); // База в порядке — отдаём управление консольному меню.
    }
}
