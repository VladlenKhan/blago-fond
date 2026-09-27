package ru.mirea.fund.repository;

import java.util.List;
import java.util.Optional;

/**
 * Общий интерфейс репозитория. Его реализуют DonorRepository и DonationRepository —
 * это даёт полиморфизм: с любым репозиторием можно работать через один тип.
 */
public interface CrudRepository<T> {

    int save(T entity);

    List<T> findAll();

    Optional<T> findById(int id);

    void update(T entity);

    void delete(int id);
}
