package ru.mirea.fund.repository; // Объявляем пакет класса.

import ru.mirea.fund.exception.DataAccessException; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donation; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationCategory; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationStatus; // Подключаем необходимый тип.
import ru.mirea.fund.util.DatabaseManager; // Подключаем необходимый тип.

import java.sql.Connection; // Подключаем необходимый тип.
import java.sql.PreparedStatement; // Подключаем необходимый тип.
import java.sql.ResultSet; // Подключаем необходимый тип.
import java.sql.SQLException; // Подключаем необходимый тип.
import java.time.LocalDate; // Подключаем необходимый тип.
import java.time.LocalDateTime; // Подключаем необходимый тип.
import java.util.ArrayList; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.
import java.util.Optional; // Подключаем необходимый тип.

/** Слой доступа к данным для таблицы donations: SQL и соединение с таблицей donors. */
public class DonationRepository implements CrudRepository<Donation> { // Репозиторий пожертвований: SQL-запросы к таблице donations.

    private static final String SELECT_BASE = // Общая часть SELECT: соединение donations с donors.
            "SELECT d.id, d.donor_id, p.full_name AS donor_name, d.purpose, d.category, d.status, d.amount, d.created_at " // Описываем часть SQL-запроса.
                    + "FROM donations d JOIN donors p ON p.id = d.donor_id "; // Соединяем таблицы.

    @Override // Переопределяем метод.
    public int save(Donation donation) { // Добавляет пожертвование и возвращает новый id.
        String sql = "INSERT INTO donations (donor_id, purpose, category, status, amount, created_at) " // Создаем переменную или объект.
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id"; // Передаем значения полей.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setInt(1, donation.getDonorId()); // Передаем параметр запроса.
            statement.setString(2, donation.getPurpose()); // Передаем параметр запроса.
            statement.setString(3, donation.getCategory().name()); // Передаем параметр запроса.
            statement.setString(4, donation.getStatus().name()); // Передаем параметр запроса.
            statement.setBigDecimal(5, donation.getAmount()); // Передаем параметр запроса.
            statement.setObject(6, donation.getCreatedAt()); // Передаем параметр запроса.

            try (ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
                rs.next(); // Переходим на строку результата.
                int id = rs.getInt(1); // Читаем сгенерированный идентификатор.
                donation.setId(id); // Сохраняем значение в объекте.
                return id; // Возвращаем результат.
            } // Завершаем блок.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось добавить пожертвование", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public List<Donation> findAll() { // Читает все пожертвования вместе с именем донора.
        return query(SELECT_BASE + "ORDER BY d.id"); // Возвращаем результат.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public Optional<Donation> findById(int id) { // Ищет пожертвование по первичному ключу.
        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE + "WHERE d.id = ?")) { // Подготавливаем параметризованный SQL-запрос.
            statement.setInt(1, id); // Передаем параметр запроса.

            try (ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); // Возвращаем результат.
            } // Завершаем блок.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось найти пожертвование", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public void update(Donation donation) { // Обновляет пожертвование, кроме даты создания.
        String sql = "UPDATE donations SET donor_id = ?, purpose = ?, category = ?, status = ?, amount = ? WHERE id = ?"; // Создаем переменную или объект.

        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setInt(1, donation.getDonorId()); // Передаем параметр запроса.
            statement.setString(2, donation.getPurpose()); // Передаем параметр запроса.
            statement.setString(3, donation.getCategory().name()); // Передаем параметр запроса.
            statement.setString(4, donation.getStatus().name()); // Передаем параметр запроса.
            statement.setBigDecimal(5, donation.getAmount()); // Передаем параметр запроса.
            statement.setInt(6, donation.getId()); // Передаем параметр запроса.
            statement.executeUpdate(); // Выполняем запрос к базе данных.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось изменить пожертвование", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public void delete(int id) { // Удаляет пожертвование по id.
        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement("DELETE FROM donations WHERE id = ?")) { // Подготавливаем параметризованный SQL-запрос.
            statement.setInt(1, id); // Передаем параметр запроса.
            statement.executeUpdate(); // Выполняем запрос к базе данных.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось удалить пожертвование", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Поиск по назначению пожертвования. */
    public List<Donation> searchByPurpose(String text) { // Ищет по части назначения без учёта регистра.
        return queryWithText(SELECT_BASE + "WHERE LOWER(d.purpose) LIKE LOWER(?) ORDER BY d.id", text); // Возвращаем результат.
    } // Завершаем блок.

    /** Поиск по имени донора. */
    public List<Donation> searchByDonorName(String text) { // Ищет по имени донора благодаря JOIN.
        return queryWithText(SELECT_BASE + "WHERE LOWER(p.full_name) LIKE LOWER(?) ORDER BY d.id", text); // Возвращаем результат.
    } // Завершаем блок.

    /** Поиск по диапазону дат. */
    public List<Donation> searchByDateRange(LocalDate from, LocalDate to) { // Отбирает пожертвования за период дат.
        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement( // Подготавливаем параметризованный SQL-запрос.
                     SELECT_BASE + "WHERE d.created_at >= ? AND d.created_at < ? ORDER BY d.created_at")) { // Фильтруем строки.
            statement.setObject(1, from.atStartOfDay()); // Передаем параметр запроса.
            statement.setObject(2, to.plusDays(1).atStartOfDay()); // Передаем параметр запроса.
            return readAll(statement); // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка поиска по датам", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Все пожертвования конкретного донора. */
    public List<Donation> findByDonorId(int donorId) { // Возвращает все пожертвования одного донора.
        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE + "WHERE d.donor_id = ? ORDER BY d.id")) { // Подготавливаем параметризованный SQL-запрос.
            statement.setInt(1, donorId); // Передаем параметр запроса.
            return readAll(statement); // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка выборки пожертвований донора", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Выполняет запрос с одним текстовым параметром. */
    private List<Donation> queryWithText(String sql, String text) { // Общий код для поисковых запросов с LIKE.
        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            statement.setString(1, "%" + text + "%"); // Передаем параметр запроса.
            return readAll(statement); // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка поиска пожертвований", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Выполняет запрос без параметров. */
    private List<Donation> query(String sql) { // Общий код для запросов без параметров.
        try (Connection connection = DatabaseManager.getConnection(); // Открываем ресурсы безопасно.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Подготавливаем параметризованный SQL-запрос.
            return readAll(statement); // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка выборки пожертвований", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Читает все строки ответа в список объектов. */
    private List<Donation> readAll(PreparedStatement statement) throws SQLException { // Читает все строки ответа в список объектов.
        List<Donation> donations = new ArrayList<>(); // Создаем переменную или объект.
        try (ResultSet rs = statement.executeQuery()) { // Выполняем запрос к базе данных.
            while (rs.next()) { // Повторяем, пока есть строки.
                donations.add(mapRow(rs)); // Добавляем элемент.
            } // Завершаем блок.
        } // Завершаем блок.
        return donations; // Возвращаем результат.
    } // Завершаем блок.

    /** Преобразует строку ResultSet в объект Donation. */
    private Donation mapRow(ResultSet rs) throws SQLException { // Превращает строку ResultSet в объект Donation.
        return new Donation( // Возвращаем результат.
                rs.getInt("id"), // Читаем значение столбца.
                rs.getInt("donor_id"), // Читаем значение столбца.
                rs.getString("donor_name"), // Читаем значение столбца.
                rs.getString("purpose"), // Читаем значение столбца.
                DonationCategory.valueOf(rs.getString("category")), // Преобразуем строку в enum.
                DonationStatus.valueOf(rs.getString("status")), // Преобразуем строку в enum.
                rs.getBigDecimal("amount"), // Читаем значение столбца.
                rs.getObject("created_at", LocalDateTime.class)); // Читаем значение столбца.
    } // Завершаем блок.
} // Завершаем блок.
