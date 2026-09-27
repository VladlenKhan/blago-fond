package ru.mirea.fund.exception; // Пакет собственных исключений системы.

// Ошибка базы данных: не удалось подключиться, не выполнился SQL-запрос и т.п.
// Зачем своё исключение, если есть SQLException: SQLException — проверяемое,
// его пришлось бы тащить через все слои (repository -> service -> ui) в сигнатурах методов.
// Вместо этого репозиторий ловит SQLException и заворачивает его сюда,
// и верхние слои про JDBC уже ничего не знают.
public class DataAccessException extends RuntimeException {

    // message — что именно пытались сделать («Не удалось добавить донора»),
    // cause — исходное SQLException: сохраняем его, чтобы не потерять настоящую причину сбоя.
    public DataAccessException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), cause);
    }
}
