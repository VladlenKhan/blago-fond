package ru.mirea.fund.repository;

import ru.mirea.fund.exception.DataAccessException;
import ru.mirea.fund.model.Donation;
import ru.mirea.fund.model.DonationCategory;
import ru.mirea.fund.model.DonationStatus;
import ru.mirea.fund.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Работа с таблицей donations. Имя донора подтягивается соединением с таблицей donors. */
public class DonationRepository implements CrudRepository<Donation> {

    private static final String SELECT_BASE =
            "SELECT d.id, d.donor_id, p.full_name AS donor_name, d.purpose, d.category, d.status, d.amount, d.created_at "
                    + "FROM donations d JOIN donors p ON p.id = d.donor_id ";

    @Override
    public int save(Donation donation) {
        String sql = "INSERT INTO donations (donor_id, purpose, category, status, amount, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, donation.getDonorId());
            statement.setString(2, donation.getPurpose());
            statement.setString(3, donation.getCategory().name());
            statement.setString(4, donation.getStatus().name());
            statement.setBigDecimal(5, donation.getAmount());
            statement.setObject(6, donation.getCreatedAt());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                donation.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось добавить пожертвование", e);
        }
    }

    @Override
    public List<Donation> findAll() {
        return query(SELECT_BASE + "ORDER BY d.id");
    }

    @Override
    public Optional<Donation> findById(int id) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE + "WHERE d.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти пожертвование", e);
        }
    }

    @Override
    public void update(Donation donation) {
        String sql = "UPDATE donations SET donor_id = ?, purpose = ?, category = ?, status = ?, amount = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, donation.getDonorId());
            statement.setString(2, donation.getPurpose());
            statement.setString(3, donation.getCategory().name());
            statement.setString(4, donation.getStatus().name());
            statement.setBigDecimal(5, donation.getAmount());
            statement.setInt(6, donation.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить пожертвование", e);
        }
    }

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

    /** Поиск по назначению пожертвования (часть текста, без учёта регистра). */
    public List<Donation> searchByPurpose(String text) {
        return queryWithText(SELECT_BASE + "WHERE LOWER(d.purpose) LIKE LOWER(?) ORDER BY d.id", text);
    }

    /** Поиск по имени донора. */
    public List<Donation> searchByDonorName(String text) {
        return queryWithText(SELECT_BASE + "WHERE LOWER(p.full_name) LIKE LOWER(?) ORDER BY d.id", text);
    }

    /** Поиск по диапазону дат. */
    public List<Donation> searchByDateRange(LocalDate from, LocalDate to) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT_BASE + "WHERE d.created_at >= ? AND d.created_at < ? ORDER BY d.created_at")) {
            statement.setObject(1, from.atStartOfDay());
            statement.setObject(2, to.plusDays(1).atStartOfDay());
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска по датам", e);
        }
    }

    /** Все пожертвования конкретного донора. */
    public List<Donation> findByDonorId(int donorId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE + "WHERE d.donor_id = ? ORDER BY d.id")) {
            statement.setInt(1, donorId);
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка выборки пожертвований донора", e);
        }
    }

    private List<Donation> queryWithText(String sql, String text) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + text + "%");
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска пожертвований", e);
        }
    }

    private List<Donation> query(String sql) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return readAll(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка выборки пожертвований", e);
        }
    }

    private List<Donation> readAll(PreparedStatement statement) throws SQLException {
        List<Donation> donations = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                donations.add(mapRow(rs));
            }
        }
        return donations;
    }

    private Donation mapRow(ResultSet rs) throws SQLException {
        return new Donation(
                rs.getInt("id"),
                rs.getInt("donor_id"),
                rs.getString("donor_name"),
                rs.getString("purpose"),
                DonationCategory.valueOf(rs.getString("category")),
                DonationStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("amount"),
                rs.getObject("created_at", LocalDateTime.class));
    }
}
