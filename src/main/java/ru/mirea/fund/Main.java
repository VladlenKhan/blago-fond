package ru.mirea.fund;

import ru.mirea.fund.exception.DataAccessException;
import ru.mirea.fund.ui.ConsoleApp;
import ru.mirea.fund.util.DatabaseManager;

/** Точка входа: проверяет базу данных и запускает консольное меню. */
public class Main {

    public static void main(String[] args) {
        try {
            DatabaseManager.checkConnection();
            DatabaseManager.initSchemaIfNeeded();
        } catch (DataAccessException e) {
            System.out.println("Ошибка: " + e.getMessage());
            System.out.println("Проверьте, что PostgreSQL запущен и настройки в db.properties корректны.");
            return;
        }
        new ConsoleApp().run();
    }
}
