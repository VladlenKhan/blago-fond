package ru.mirea.fund.repository; // Пакет слоя доступа к данным: здесь живёт весь SQL.

import ru.mirea.fund.exception.DataAccessException; // Своё исключение: в него заворачиваем SQLException.
import ru.mirea.fund.model.Donation; // Класс-сущность, строки таблицы превращаются в его объекты.
import ru.mirea.fund.model.DonationCategory; // Enum направления: из базы приходит строкой, обратно превращаем в константу.
import ru.mirea.fund.model.DonationStatus; // Enum статуса: из базы приходит строкой, обратно превращаем в константу.
import ru.mirea.fund.util.DatabaseManager; // Выдаёт соединение с базой по настройкам из db.properties.

import java.sql.Connection; // Сеанс связи с базой данных: через него выполняются все запросы.
import java.sql.PreparedStatement; // Параметризованный запрос: значения подставляются вместо «?», инъекция невозможна.
import java.sql.ResultSet; // Курсор по строкам ответа базы: из него читаем значения столбцов.
import java.sql.SQLException; // Проверяемая ошибка JDBC: обязаны её перехватить.
import java.time.LocalDate; // Дата без времени: её вводит пользователь при поиске за период.
import java.time.LocalDateTime; // Дата со временем: в таком виде хранится столбец created_at.
import java.util.ArrayList; // Реализация списка, в который складываем прочитанные записи.
import java.util.List; // Тип возвращаемого набора записей.
import java.util.Optional; // Результат поиска одной записи: есть значение или нет.

// Слой доступа к данным для таблицы donations.
// Почти во всех запросах есть JOIN с donors: в самой таблице хранится только donor_id,
// а на экран нужно выводить имя донора.
public class DonationRepository implements CrudRepository<Donation> {

    // Общая часть всех SELECT-запросов вынесена в константу, чтобы не копировать
    // длинный JOIN в каждый метод — дальше к ней просто дописывается WHERE или ORDER BY.
    // d — псевдоним таблицы donations, p — таблицы donors.
    private static final String SELECT_BASE =
            "SELECT d.id, d.donor_id, p.full_name AS donor_name, d.purpose, d.category, d.status, d.amount, d.created_at "
                    + "FROM donations d JOIN donors p ON p.id = d.donor_id ";

    // Добавляет пожертвование в базу и возвращает присвоенный ему id.
    @Override
    public int save(Donation donation) {
        String sql = "INSERT INTO donations (donor_id, purpose, category, status, amount, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id"; // RETURNING id сразу отдаёт сгенерированный первичный ключ.

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, donation.getDonorId());
            statement.setString(2, donation.getPurpose());
            // В базе enum хранится строкой, поэтому берём name(): MEDICINE, NEW и т.д.
            // На эти столбцы стоит CHECK — база не примет чужое значение.
            statement.setString(3, donation.getCategory().name());
            statement.setString(4, donation.getStatus().name());
            statement.setBigDecimal(5, donation.getAmount()); // Для денег отдельный метод setBigDecimal, а не setDouble.
            statement.setObject(6, donation.getCreatedAt());

            try (ResultSet rs = statement.executeQuery()) {
                rs.next(); // Переходим на единственную строку ответа с новым id.
                int id = rs.getInt(1);
                donation.setId(id); // Проставляем id объекту в памяти, чтобы он совпадал с записью в базе.
                return id;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось добавить пожертвование", e);
        }
    }

    // Читает все пожертвования вместе с именем донора.
    @Override
    public List<Donation> findAll() {
        return query(SELECT_BASE + "ORDER BY d.id");
    }

    // Ищет пожертвование по первичному ключу.
    @Override
    public Optional<Donation> findById(int id) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE + "WHERE d.id = ?")) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); // Строка есть — заворачиваем в Optional, нет — отдаём пустой.
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти пожертвование", e);
        }
    }

    // Сохраняет изменённые данные пожертвования.
    @Override
    public void update(Donation donation) {
        // created_at в запрос не входит: дата создания записи неизменна.
        String sql = "UPDATE donations SET donor_id = ?, purpose = ?, category = ?, status = ?, amount = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, donation.getDonorId());
            statement.setString(2, donation.getPurpose());
            statement.setString(3, donation.getCategory().name());
            statement.setString(4, donation.getStatus().name());
            statement.setBigDecimal(5, donation.getAmount());
            statement.setInt(6, donation.getId()); // Шестой параметр — это «?» в условии WHERE id = ?.
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить пожертвование", e);
        }
    }

    // Удаляет пожертвование по id.
    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM donations WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить пожертвование", e);
        }
    }

    // ПОИСК 1: по назначению пожертвования, без учёта регистра.
    public List<Donation> searchByPurpose(String text) {
        return queryWithText(SELECT_BASE + "WHERE LOWER(d.purpose) LIKE LOWER(?) ORDER BY d.id", text);
    }

    // ПОИСК 2: по имени донора — работает благодаря JOIN с таблицей donors.
    public List<Donation> searchByDonorName(String text) {
        return queryWithText(SELECT_BASE + "WHERE LOWER(p.full_name) LIKE LOWER(?) ORDER BY d.id", text);
    }

    // ПОИСК 3: по диапазону дат.
    public List<Donation> searchByDateRange(LocalDate from, LocalDate to) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT_BASE + "WHERE d.created_at >= ? AND d.created_at < ? ORDER BY d.created_at")) {

            // В created_at лежит время с часами и минутами, а пользователь вводит только дату.
            // Поэтому берём интервал [начало дня «от»; начало следующего дня после «до»),
            // иначе записи за последний день со временем 15:30 в результат не попали бы.
            statement.setObject(1, from.atStartOfDay());
            statement.setObject(2, to.plusDays(1).atStartOfDay());
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска по датам", e);
        }
    }

    // Возвращает все пожертвования одного донора — это фильтр по донору.
    public List<Donation> findByDonorId(int donorId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE + "WHERE d.donor_id = ? ORDER BY d.id")) {
            statement.setInt(1, donorId);
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка выборки пожертвований донора", e);
        }
    }

    // Выполняет поисковый запрос с одним текстовым параметром для LIKE.
    // Общий код для searchByPurpose и searchByDonorName, чтобы не дублировать try-with-resources.
    private List<Donation> queryWithText(String sql, String text) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + text + "%"); // Проценты добавляем к значению параметра, а не к тексту запроса.
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска пожертвований", e);
        }
    }

    // Выполняет запрос без параметров.
    private List<Donation> query(String sql) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка выборки пожертвований", e);
        }
    }

    // Прогоняет курсор по всем строкам ответа и собирает из них список объектов.
    private List<Donation> readAll(PreparedStatement statement) throws SQLException {
        List<Donation> donations = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                donations.add(mapRow(rs));
            }
        }
        return donations;
    }

    // Превращает текущую строку ResultSet в объект Donation.
    private Donation mapRow(ResultSet rs) throws SQLException {
        return new Donation(
                rs.getInt("id"),
                rs.getInt("donor_id"),
                rs.getString("donor_name"), // Это поле пришло из JOIN с таблицей donors.
                rs.getString("purpose"),
                // Строка из базы превращается обратно в enum. Здесь valueOf безопасен:
                // значения пришли из базы, а там стоит CHECK, поэтому мусора быть не может.
                DonationCategory.valueOf(rs.getString("category")),
                DonationStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("amount"),
                rs.getObject("created_at", LocalDateTime.class)); // Читаем TIMESTAMP сразу в LocalDateTime.
    }
}
