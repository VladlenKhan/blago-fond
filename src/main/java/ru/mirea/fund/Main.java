package ru.mirea.fund; // Объявляем пакет класса.

import ru.mirea.fund.exception.DataAccessException; // Подключаем необходимый тип.
import ru.mirea.fund.ui.ConsoleApp; // Подключаем необходимый тип.
import ru.mirea.fund.util.DatabaseManager; // Подключаем необходимый тип.

/** Точка входа: проверяет базу данных и запускает консольное меню. */
public class Main { // Точка входа: только запуск, вся логика в других классах.

    public static void main(String[] args) { // Точка входа в программу.
        try { // Открываем блок обработки ошибок.
            DatabaseManager.checkConnection(); // Проверяем подключение к базе данных.
            DatabaseManager.initSchemaIfNeeded(); // Создаем таблицы при первом запуске.
        } catch (DataAccessException e) { // Обрабатываем исключение.
            System.out.println("Ошибка: " + e.getMessage()); // Выводим результат в консоль.
            System.out.println("Проверьте, что PostgreSQL запущен и настройки в db.properties корректны."); // Выводим результат в консоль.
            return; // Завершаем выполнение метода.
        } // Завершаем блок.

        new ConsoleApp().run(); // Запускаем консольное меню.
    } // Завершаем блок.
} // Завершаем блок.
