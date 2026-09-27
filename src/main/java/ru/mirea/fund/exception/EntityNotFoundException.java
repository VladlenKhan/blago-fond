package ru.mirea.fund.exception; // Пакет собственных исключений системы.

// Записи с указанным ID в базе нет.
// Бросается, когда пользователь ввёл несуществующий номер донора или пожертвования:
// сервис получает из репозитория пустой Optional и превращает его в эту ошибку.
public class EntityNotFoundException extends RuntimeException {

    // Конструктор сам собирает текст из названия сущности и id,
    // чтобы в сервисах не повторять одну и ту же строку:
    // new EntityNotFoundException("Донор", 99) даст «Донор с ID 99 не найден(о)».
    public EntityNotFoundException(String entity, int id) {
        super(entity + " с ID " + id + " не найден(о)");
    }
}
