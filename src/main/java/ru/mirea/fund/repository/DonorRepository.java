package ru.mirea.fund.repository;

import ru.mirea.fund.exception.DataAccessException;
import ru.mirea.fund.model.Donor;
import ru.mirea.fund.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Работа с таблицей donors. Только SQL, без бизнес-логики. */
public class DonorRepository implements CrudRepository<Donor> {

    @Override
    public int save(Donor donor) {
        String sql = "INSERT INTO donors (full_name, email, phone, city, registered_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, donor.getFullName());
            statement.setString(2, donor.getEmail());
            statement.setString(3, donor.getPhone());
            statement.setString(4, donor.getCity());
            statement.setObject(5, donor.getRegisteredAt());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                donor.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось добавить донора", e);
        }
    }

    @Override
    public List<Donor> findAll() {
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors ORDER BY id";
        List<Donor> donors = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                donors.add(mapRow(rs));
            }
            return donors;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список доноров", e);
        }
    }

    @Override
    public Optional<Donor> findById(int id) {
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти донора", e);
        }
    }

    @Override
    public void update(Donor donor) {
        String sql = "UPDATE donors SET full_name = ?, email = ?, phone = ?, city = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, donor.getFullName());
            statement.setString(2, donor.getEmail());
            statement.setString(3, donor.getPhone());
            statement.setString(4, donor.getCity());
            statement.setInt(5, donor.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить донора", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM donors WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить донора", e);
        }
    }

    /** Поиск донора по части имени или email (параметризованный LIKE). */
    public List<Donor> searchByText(String text) {
        String sql = "SELECT id, full_name, email, phone, city, registered_at FROM donors "
                + "WHERE LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) ORDER BY full_name";
        List<Donor> donors = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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

    /** Проверка уникальности email (бизнес-правило проверяется в сервисе). */
    public boolean existsByEmail(String email, int exceptId) {
        String sql = "SELECT 1 FROM donors WHERE LOWER(email) = LOWER(?) AND id <> ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setInt(2, exceptId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка проверки email", e);
        }
    }

    private Donor mapRow(ResultSet rs) throws SQLException {
        return new Donor(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("city"),
                rs.getObject("registered_at", java.time.LocalDate.class));
    }
}
