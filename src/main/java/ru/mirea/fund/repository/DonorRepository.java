package ru.mirea.fund.repository; // Объявляем пакет класса.

import ru.mirea.fund.exception.DataAccessException; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donor; // Подключаем необходимый тип.
import ru.mirea.fund.util.DatabaseManager; // Подключаем необходимый тип.

import java.sql.Connection; // Подключаем необходимый тип.
import java.sql.PreparedStatement; // Подключаем необходимый тип.
import java.sql.ResultSet; // Подключаем необходимый тип.
import java.sql.SQLException; // Подключаем необходимый тип.
import java.time.LocalDate; // Подключаем необходимый тип.
import java.util.ArrayList; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.
import java.util.Optional; // Подключаем необходимый тип.

/** Слой доступа к данным для таблицы donors: только SQL, без бизнес-логики. */
public class DonorRepository implements CrudRepository<Donor> { // Репозиторий доноров: выполняет SQL-запросы к таблице donors.

    @Override // Переопределяем метод.
    public int save(Donor donor) { // Добавляет донора в базу и возвращает новый id.
        String sql = "INSERT INTO donors (full_name, email, phone, city, registered_at) VALUES (?, ?, ?, ?, ?) RETURNING id"; // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setString(1, donor.getFullName()); // Передаем параметр запроса.
            statement.setString(2, donor.getEmail()); // Передаем параметр запроса.
            statement.setString(3, donor.getPhone()); // Передаем параметр запроса.
            statement.setString(4, donor.getCity()); // Передаем параметр запроса.
            statement.setObject(5, donor.getRegisteredAt()); // Передаем параметр запроса.

            try (ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
                rs.next(); // Переходим на строку результата.
                int id = rs.getInt(1); // Читаем сгенерированный идентификатор.
                donor.setId(id); // Сохраняем значение в объекте.
                return id; // Возвращаем результат.
            } // Завершаем блок.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось добавить донора", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public List<Donor> findAll() { // Читает всех доноров, упорядочив по id.
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors ORDER BY id"; // Создаем переменную или объект.
        List<Donor> donors = new ArrayList<>(); // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql); // Подготавливаем параметризованный SQL-запрос.
             ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
            while (rs.next()) { // Повторяем, пока есть строки.
                donors.add(mapRow(rs)); // Добавляем элемент.
            } // Завершаем блок.
            return donors; // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось получить список доноров", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public Optional<Donor> findById(int id) { // Ищет донора по первичному ключу.
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors WHERE id = ?"; // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setInt(1, id); // Передаем параметр запроса.

            try (ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); // Возвращаем результат.
            } // Завершаем блок.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось найти донора", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public void update(Donor donor) { // Обновляет данные донора, кроме даты регистрации.
        String sql = "UPDATE donors SET full_name = ?, email = ?, phone = ?, city = ? WHERE id = ?"; // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setString(1, donor.getFullName()); // Передаем параметр запроса.
            statement.setString(2, donor.getEmail()); // Передаем параметр запроса.
            statement.setString(3, donor.getPhone()); // Передаем параметр запроса.
            statement.setString(4, donor.getCity()); // Передаем параметр запроса.
            statement.setInt(5, donor.getId()); // Передаем параметр запроса.
            statement.executeUpdate(); // Выполняем запрос к базе данных.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось изменить донора", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public void delete(int id) { // Удаляет донора; пожертвования уходят каскадом.
        String sql = "DELETE FROM donors WHERE id = ?"; // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setInt(1, id); // Передаем параметр запроса.
            statement.executeUpdate(); // Выполняем запрос к базе данных.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось удалить донора", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Поиск донора по части имени или email. */
    public List<Donor> searchByText(String text) { // Ищет донора по части имени или email через LIKE.
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors " // Создаем переменную или объект.
                + "WHERE LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) ORDER BY full_name"; // Описываем часть SQL-запроса.
        List<Donor> donors = new ArrayList<>(); // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setString(1, "%" + text + "%"); // Передаем параметр запроса.
            statement.setString(2, "%" + text + "%"); // Передаем параметр запроса.

            try (ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
                while (rs.next()) { // Повторяем, пока есть строки.
                    donors.add(mapRow(rs)); // Добавляем элемент.
                } // Завершаем блок.
            } // Завершаем блок.
            return donors; // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка поиска доноров", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Проверка занятости email другим донором. */
    public boolean existsByEmail(String email, int exceptId) { // Проверяет, занят ли email другим донором.
        String sql = "SELECT 1 FROM donors WHERE LOWER(email) = LOWER(?) AND id <> ?"; // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setString(1, email); // Передаем параметр запроса.
            statement.setInt(2, exceptId); // Передаем параметр запроса.

            try (ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
                return rs.next(); // Возвращаем результат.
            } // Завершаем блок.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка проверки email", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Преобразует строку ResultSet в объект Donor. */
    private Donor mapRow(ResultSet rs) throws SQLException { // Превращает строку ResultSet в объект Donor.
        return new Donor( // Возвращаем результат.
                rs.getInt("id"), // Читаем значение столбца.
                rs.getString("full_name"), // Читаем значение столбца.
                rs.getString("email"), // Читаем значение столбца.
                rs.getString("phone"), // Читаем значение столбца.
                rs.getString("city"), // Читаем значение столбца.
                rs.getObject("registered_at", LocalDate.class)); // Читаем значение столбца.
    } // Завершаем блок.
} // Завершаем блок.
