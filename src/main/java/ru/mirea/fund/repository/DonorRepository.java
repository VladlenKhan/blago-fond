package ru.mirea.fund.repository; // Пакет слоя доступа к данным: здесь живёт весь SQL.

import ru.mirea.fund.exception.DataAccessException; // Своё исключение: в него заворачиваем SQLException.
import ru.mirea.fund.model.Donor; // Класс-сущность, строки таблицы превращаются в его объекты.
import ru.mirea.fund.util.DatabaseManager; // Выдаёт соединение с базой по настройкам из db.properties.

import java.sql.Connection; // Сеанс связи с базой данных: через него выполняются все запросы.
import java.sql.PreparedStatement; // Параметризованный запрос: значения подставляются вместо «?», инъекция невозможна.
import java.sql.ResultSet; // Курсор по строкам ответа базы: из него читаем значения столбцов.
import java.sql.SQLException; // Проверяемая ошибка JDBC: обязаны её перехватить.
import java.time.LocalDate; // Тип для чтения столбца DATE без устаревшего java.sql.Date.
import java.util.ArrayList; // Реализация списка, в который складываем прочитанные записи.
import java.util.List; // Тип возвращаемого набора записей.
import java.util.Optional; // Результат поиска одной записи: есть значение или нет.

// Слой доступа к данным для таблицы donors.
// Здесь только SQL: никаких проверок вроде «email уже занят» — это работа сервиса.
// Репозиторий выполняет запрос и возвращает данные.
public class DonorRepository implements CrudRepository<Donor> {

    // Добавляет донора в базу и возвращает присвоенный ему id.
    @Override
    public int save(Donor donor) {
        // RETURNING id — приём PostgreSQL: INSERT сразу отдаёт сгенерированный
        // первичный ключ, поэтому отдельный SELECT не нужен.
        String sql = "INSERT INTO donors (full_name, email, phone, city, registered_at) VALUES (?, ?, ?, ?, ?) RETURNING id";

        // try-with-resources: Connection и PreparedStatement закроются сами при выходе
        // из блока, даже если вылетит исключение — соединения не «утекут».
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Подставляем значения вместо знаков «?». Нумерация параметров идёт с 1, а не с 0.
            // Строка пользователя никогда не склеивается с текстом запроса, поэтому SQL-инъекция невозможна.
            statement.setString(1, donor.getFullName());
            statement.setString(2, donor.getEmail());
            statement.setString(3, donor.getPhone());
            statement.setString(4, donor.getCity());
            statement.setObject(5, donor.getRegisteredAt());

            try (ResultSet rs = statement.executeQuery()) {
                rs.next(); // Переходим на единственную строку ответа с новым id.
                int id = rs.getInt(1); // Читаем значение, которое вернул RETURNING.
                donor.setId(id); // Проставляем id объекту в памяти, чтобы он совпадал с записью в базе.
                return id;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось добавить донора", e); // Не пускаем SQLException выше: заворачиваем в своё исключение.
        }
    }

    // Читает всех доноров, упорядочив их по id.
    @Override
    public List<Donor> findAll() {
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors ORDER BY id";
        List<Donor> donors = new ArrayList<>(); // Коллекция, в которую сложим результат.

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            // ResultSet — курсор по строкам ответа: next() двигает его на следующую строку
            // и возвращает false, когда строки закончились.
            while (rs.next()) {
                donors.add(mapRow(rs));
            }
            return donors;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список доноров", e);
        }
    }

    // Ищет донора по первичному ключу.
    @Override
    public Optional<Donor> findById(int id) {
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); // Строка есть — заворачиваем в Optional, нет — отдаём пустой.
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти донора", e);
        }
    }

    // Сохраняет изменённые данные донора.
    @Override
    public void update(Donor donor) {
        // registered_at в запрос не входит: дату регистрации задним числом не меняем.
        String sql = "UPDATE donors SET full_name = ?, email = ?, phone = ?, city = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, donor.getFullName());
            statement.setString(2, donor.getEmail());
            statement.setString(3, donor.getPhone());
            statement.setString(4, donor.getCity());
            statement.setInt(5, donor.getId()); // Пятый параметр — это «?» в условии WHERE id = ?.

            // executeUpdate (а не executeQuery) используется для INSERT, UPDATE и DELETE:
            // он возвращает количество изменённых строк, а не таблицу.
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить донора", e);
        }
    }

    // Удаляет донора по id.
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM donors WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate(); // Пожертвования этого донора удалятся сами: во внешнем ключе указано ON DELETE CASCADE.
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить донора", e);
        }
    }

    // ПОИСК донора по части имени или email.
    // LIKE ищет по шаблону, а LOWER приводит обе стороны к нижнему регистру,
    // чтобы запрос «иванов» находил запись «Иванов».
    public List<Donor> searchByText(String text) {
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors "
                + "WHERE LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) ORDER BY full_name";
        List<Donor> donors = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Проценты добавляем к значению параметра, а не к тексту запроса,
            // поэтому запрос остаётся параметризованным и безопасным.
            statement.setString(1, "%" + text + "%");
            statement.setString(2, "%" + text + "%");

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    donors.add(mapRow(rs));
                }
            }
            return donors;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска доноров", e);
        }
    }

    // Проверяет, есть ли уже донор с таким email, не считая донора с id = exceptId.
    // exceptId нужен при редактировании: донор может сохранить свой же email.
    // При создании передаём 0 — такого id не существует, значит проверяются все записи.
    public boolean existsByEmail(String email, int exceptId) {
        String sql = "SELECT 1 FROM donors WHERE LOWER(email) = LOWER(?) AND id <> ?"; // SELECT 1: сами данные не нужны, важен только факт наличия строки.

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setInt(2, exceptId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next(); // true, если хотя бы одна строка нашлась.
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка проверки email", e);
        }
    }

    // Превращает текущую строку ResultSet в объект Donor.
    // Вынесено в отдельный метод, чтобы не дублировать этот код в findAll, findById и searchByText.
    private Donor mapRow(ResultSet rs) throws SQLException {
        return new Donor(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("city"),
                rs.getObject("registered_at", LocalDate.class)); // Читаем DATE сразу в LocalDate.
    }
}
