package ru.mirea.fund.util; // Объявляем пакет класса.

import ru.mirea.fund.exception.DataAccessException; // Подключаем необходимый тип.

import java.io.IOException; // Подключаем необходимый тип.
import java.io.InputStream; // Подключаем необходимый тип.
import java.sql.Connection; // Подключаем необходимый тип.
import java.sql.DatabaseMetaData; // Подключаем необходимый тип.
import java.sql.DriverManager; // Подключаем необходимый тип.
import java.sql.ResultSet; // Подключаем необходимый тип.
import java.sql.SQLException; // Подключаем необходимый тип.
import java.sql.Statement; // Подключаем необходимый тип.
import java.util.ArrayList; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.
import java.util.Properties; // Подключаем необходимый тип.
import java.util.Scanner; // Подключаем необходимый тип.

/** Единая точка подключения к PostgreSQL через JDBC. Настройки — в db.properties. */
public class DatabaseManager { // Точка подключения к БД: все методы статические.

    private static final String url; // Адрес базы данных из db.properties.
    private static final String user; // Пользователь БД из db.properties.
    private static final String password; // Пароль БД из db.properties.

    static { // Выполняем статическую инициализацию.
        Properties props = new Properties(); // Создаем переменную или объект.
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/db.properties")) { // Открываем ресурсы безопасно.
            props.load(in); // Читаем настройки из файла.
        } catch (IOException | NullPointerException e) { // Обрабатываем исключение.
            throw new RuntimeException("Не удалось прочитать файл настроек db.properties", e); // Выбрасываем исключение.
        } // Завершаем блок.
        url = props.getProperty("db.url"); // Сохраняем адрес базы данных.
        user = props.getProperty("db.user"); // Сохраняем пользователя БД.
        password = props.getProperty("db.password"); // Сохраняем пароль БД.
    } // Завершаем блок.

    private DatabaseManager() { // Приватный конструктор: объекты этого класса не нужны.
    } // Завершаем блок.

    /** Открывает новое соединение с базой данных. */
    public static Connection getConnection() { // Выдаёт новое соединение; закрывает его вызывающий код.
        try { // Открываем блок обработки ошибок.
            return DriverManager.getConnection(url, user, password); // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Не удалось подключиться к базе данных " + url, e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Проверяет связь с базой при старте программы. */
    public static void checkConnection() { // Проверяет доступность базы при старте программы.
        try (Connection connection = getConnection()) { // Открываем ресурсы безопасно.
            System.out.println("Подключение к базе данных установлено: " + url); // Выводим результат в консоль.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка подключения к базе данных", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Создаёт таблицы и тестовые данные, если их ещё нет. */
    public static void initSchemaIfNeeded() { // Выполняет schema.sql, если таблиц ещё нет.
        if (tableExists("donations")) { // Проверяем условие.
            return; // Завершаем выполнение метода.
        } // Завершаем блок.

        System.out.println("Таблицы не найдены, создаю схему и тестовые данные..."); // Выводим результат в консоль.
        String script = readResource("/schema.sql"); // Создаем переменную или объект.

        try (Connection connection = getConnection(); // Открываем ресурсы безопасно.
             Statement statement = connection.createStatement()) { // Создаем объект выполнения запроса.
            statement.execute(script); // Выполняем запрос к базе данных.
            System.out.println("База данных успешно инициализирована."); // Выводим результат в консоль.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка выполнения schema.sql", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Проверяет наличие таблицы через метаданные JDBC. */
    private static boolean tableExists(String table) { // Спрашивает у метаданных, есть ли такая таблица.
        try (Connection connection = getConnection(); // Открываем ресурсы безопасно.
             ResultSet rs = connection.getMetaData().getTables(null, "public", table, null)) { // Читаем метаданные базы данных.
            return rs.next(); // Возвращаем результат.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка чтения метаданных базы данных", e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Читает текстовый файл из ресурсов целиком. */
    private static String readResource(String name) { // Читает файл из ресурсов одной строкой.
        try (InputStream in = DatabaseManager.class.getResourceAsStream(name); // Открываем ресурсы безопасно.
             Scanner scanner = new Scanner(in, "UTF-8")) { // Создаем объект чтения текста.
            return scanner.useDelimiter("\\A").next(); // Возвращаем результат.
        } catch (IOException | NullPointerException e) { // Обрабатываем исключение.
            throw new RuntimeException("Не удалось прочитать файл " + name, e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Возвращает список таблиц базы с колонками и количеством строк. */
    public static List<String> describeTables() { // Собирает структуру БД для пункта меню.
        List<String> lines = new ArrayList<>(); // Создаем переменную или объект.

        try (Connection connection = getConnection()) { // Открываем ресурсы безопасно.
            DatabaseMetaData meta = connection.getMetaData(); // Читаем метаданные базы данных.

            try (ResultSet tables = meta.getTables(null, "public", "%", new String[]{"TABLE"})) { // Получаем список таблиц.
                while (tables.next()) { // Повторяем, пока есть строки.
                    String table = tables.getString("TABLE_NAME"); // Читаем имя таблицы.
                    lines.add("Таблица: " + table + " (строк: " + countRows(connection, table) + ")"); // Добавляем элемент.

                    try (ResultSet columns = meta.getColumns(null, "public", table, "%")) { // Получаем список столбцов.
                        while (columns.next()) { // Повторяем, пока есть строки.
                            lines.add(String.format("    %-15s %s%s", // Добавляем элемент.
                                    columns.getString("COLUMN_NAME"), // Читаем имя столбца.
                                    columns.getString("TYPE_NAME"), // Читаем тип столбца.
                                    "NO".equals(columns.getString("IS_NULLABLE")) ? " NOT NULL" : "")); // Отмечаем обязательность.
                        } // Завершаем блок.
                    } // Завершаем блок.
                } // Завершаем блок.
            } // Завершаем блок.
        } catch (SQLException e) { // Обрабатываем исключение.
            throw new DataAccessException("Ошибка чтения структуры базы данных", e); // Выбрасываем исключение.
        } // Завершаем блок.
        return lines; // Возвращаем результат.
    } // Завершаем блок.

    /** Считает количество строк в таблице. */
    private static int countRows(Connection connection, String table) throws SQLException { // Считает строки таблицы через COUNT(*).
        try (Statement statement = connection.createStatement(); // Создаем объект выполнения запроса.
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) { // Выполняем запрос к базе данных.
            return rs.next() ? rs.getInt(1) : 0; // Возвращаем результат.
        } // Завершаем блок.
    } // Завершаем блок.
} // Завершаем блок.
