package ru.mirea.fund.repository; // Объявляем пакет класса.

import java.util.List; // Подключаем необходимый тип.
import java.util.Optional; // Подключаем необходимый тип.

/** Общий интерфейс репозитория: базовые операции CRUD для любой сущности. */
public interface CrudRepository<T> { // Контракт репозитория: один набор операций для всех сущностей.

    int save(T entity); // Сохраняет новую запись и возвращает её id.

    List<T> findAll(); // Возвращает все записи таблицы.

    Optional<T> findById(int id); // Ищет запись по id, Optional вместо null.

    void update(T entity); // Обновляет существующую запись.

    void delete(int id); // Удаляет запись по id.
} // Завершаем блок.
