package ru.mirea.fund.exception; // Объявляем пакет класса.

/** Собственное исключение: записи с указанным ID нет в базе данных. */
public class EntityNotFoundException extends RuntimeException { // Ошибка: записи с таким ID нет в базе.

    public EntityNotFoundException(String entity, int id) { // Конструктор: сам собирает текст сообщения.
        super(entity + " с ID " + id + " не найден(о)"); // Вызываем конструктор суперкласса.
    } // Завершаем блок.
} // Завершаем блок.
