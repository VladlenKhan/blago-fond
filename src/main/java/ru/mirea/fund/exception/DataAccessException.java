package ru.mirea.fund.exception; // Объявляем пакет класса.

/** Собственное исключение: ошибка подключения к БД или выполнения SQL-запроса. */
public class DataAccessException extends RuntimeException { // Ошибка БД: обёртка над SQLException.

    public DataAccessException(String message, Throwable cause) { // Конструктор: хранит описание и исходную причину.
        super(message + ": " + cause.getMessage(), cause); // Вызываем конструктор суперкласса.
    } // Завершаем блок.
} // Завершаем блок.
