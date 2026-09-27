package ru.mirea.fund.exception;

/** Ошибка подключения к БД или выполнения SQL-запроса (обёртка над SQLException). */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), cause);
    }
}
