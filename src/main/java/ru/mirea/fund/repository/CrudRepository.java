package ru.mirea.fund.repository; // Пакет слоя доступа к данным: здесь живёт весь SQL.

import java.util.List; // Список: репозиторий возвращает набор записей таблицы.
import java.util.Optional; // Контейнер «значение есть или его нет»: безопаснее, чем возвращать null.

// Общий интерфейс репозитория — набор из пяти базовых операций CRUD
// (Create, Read, Update, Delete).
// Интерфейс обобщённый: <T> — тип сущности. DonorRepository реализует CrudRepository<Donor>,
// DonationRepository — CrudRepository<Donation>. Получается один «договор» для обоих,
// и с ними можно работать через общий тип — это полиморфизм.
public interface CrudRepository<T> {

    int save(T entity); // Вставляет новую запись и возвращает id, который присвоила база.

    List<T> findAll(); // Читает все записи таблицы для вывода, фильтров и статистики.

    // Ищет запись по id. Возвращаем Optional, а не null: вызывающий код обязан явно
    // обработать случай «не нашли», и случайного NullPointerException не будет.
    Optional<T> findById(int id);

    void update(T entity); // Сохраняет изменения объекта в уже существующую строку таблицы.

    void delete(int id); // Удаляет строку по первичному ключу.
}
