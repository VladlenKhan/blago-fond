package ru.mirea.fund.util;

import ru.mirea.fund.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/** Единая точка подключения к PostgreSQL через JDBC. Настройки читаются из db.properties. */
public class DatabaseManager {

    private static final String url;
    private static final String user;
    private static final String password;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/db.properties")) {
            props.load(in);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Не удалось прочитать файл настроек db.properties", e);
        }
        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");
    }

    private DatabaseManager() {
    }

    /** Новое соединение с базой данных. Вызывающий код закрывает его через try-with-resources. */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось подключиться к базе данных " + url, e);
        }
    }

    /** Проверка подключения при старте программы. */
    public static void checkConnection() {
        try (Connection connection = getConnection()) {
            System.out.println("Подключение к базе данных установлено: " + url);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка подключения к базе данных", e);
        }
    }

    /** Если таблиц ещё нет — создаёт их и загружает тестовые данные из schema.sql. */
    public static void initSchemaIfNeeded() {
        if (tableExists("donations")) {
            return;
        }
        System.out.println("Таблицы не найдены, создаю схему и тестовые данные...");
        String script = readResource("/schema.sql");
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(script);
            System.out.println("База данных успешно инициализирована.");
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка выполнения schema.sql", e);
        }
    }

    private static boolean tableExists(String table) {
        try (Connection connection = getConnection();
             ResultSet rs = connection.getMetaData().getTables(null, "public", table, null)) {
            return rs.next();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка чтения метаданных базы данных", e);
        }
    }

    private static String readResource(String name) {
        try (InputStream in = DatabaseManager.class.getResourceAsStream(name);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            return scanner.useDelimiter("\\A").next();
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Не удалось прочитать файл " + name, e);
        }
    }

    /** Список таблиц базы данных с колонками и количеством строк (пункт меню "Вывести таблицы БД"). */
    public static List<String> describeTables() {
        List<String> lines = new ArrayList<>();
        try (Connection connection = getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet tables = meta.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String table = tables.getString("TABLE_NAME");
                    lines.add("Таблица: " + table + " (строк: " + countRows(connection, table) + ")");
                    try (ResultSet columns = meta.getColumns(null, "public", table, "%")) {
                        while (columns.next()) {
                            lines.add(String.format("    %-15s %s%s",
                                    columns.getString("COLUMN_NAME"),
                                    columns.getString("TYPE_NAME"),
                                    "NO".equals(columns.getString("IS_NULLABLE")) ? " NOT NULL" : ""));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка чтения структуры базы данных", e);
        }
        return lines;
    }

    private static int countRows(Connection connection, String table) throws SQLException {
        // имя таблицы получено из метаданных БД, пользовательский ввод сюда не попадает
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
