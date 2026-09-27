package ru.mirea.fund.exception;

/** Записи с указанным ID нет в базе данных. */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, int id) {
        super(entity + " с ID " + id + " не найден(о)");
    }
}
