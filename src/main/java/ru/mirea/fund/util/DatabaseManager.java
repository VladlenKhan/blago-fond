package ru.mirea.fund.util; // Пакет вспомогательных классов: подключение к базе и экспорт данных.

import ru.mirea.fund.exception.DataAccessException; // Своё исключение: в него заворачиваем ошибки JDBC.

import java.io.IOException; // Ошибка чтения файла: может возникнуть при загрузке настроек и schema.sql.
import java.io.InputStream; // Поток чтения файла из ресурсов собранного jar.
import java.sql.Connection; // Сеанс связи с базой данных — то, что этот класс и выдаёт.
import java.sql.DatabaseMetaData; // Сведения о самой базе: список таблиц и их столбцов.
import java.sql.DriverManager; // Устанавливает соединение по адресу, логину и паролю.
import java.sql.ResultSet; // Курсор по строкам ответа: здесь читаем и метаданные тоже.
import java.sql.SQLException; // Проверяемая ошибка JDBC: обязаны её перехватить.
import java.sql.Statement; // Простой запрос без параметров: нужен для schema.sql и COUNT(*).
import java.util.ArrayList; // Реализация списка строк с описанием структуры базы.
import java.util.List; // Тип результата метода describeTables.
import java.util.Properties; // Читает файл вида «ключ=значение» — так устроен db.properties.
import java.util.Scanner; // Позволяет прочитать schema.sql целиком одной строкой.

// Единая точка подключения к PostgreSQL через JDBC.
// Все методы статические: объект этого класса создавать незачем, он просто выдаёт соединения.
// Логин, пароль и адрес базы лежат не в коде, а в файле db.properties — чтобы сменить пароль,
// перекомпилировать проект не нужно.
public class DatabaseManager {

    private static final String url; // Адрес базы, например jdbc:postgresql://localhost:5432/charity_fund.
    private static final String user; // Имя пользователя PostgreSQL.
    private static final String password; // Пароль пользователя PostgreSQL.

    // Статический блок инициализации выполняется ОДИН РАЗ при первом обращении к классу.
    // Здесь читаем настройки подключения из db.properties.
    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/db.properties")) { // getResourceAsStream ищет файл внутри собранного jar, в папке resources.
            props.load(in);
        } catch (IOException | NullPointerException e) {
            // NullPointerException ловим на случай, если файла вообще нет:
            // тогда getResourceAsStream вернёт null и load() упадёт.
            throw new RuntimeException("Не удалось прочитать файл настроек db.properties", e);
        }
        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");
    }

    private DatabaseManager() { // Приватный конструктор запрещает создавать объекты этого класса.
    }

    // Выдаёт НОВОЕ соединение с базой.
    // Закрывать его должен тот, кто вызвал: во всех репозиториях для этого используется try-with-resources.
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось подключиться к базе данных " + url, e);
        }
    }

    // Проверка связи с базой при старте программы (вызывается из Main).
    // Соединение открываем и тут же закрываем: важен сам факт, что подключение проходит.
    public static void checkConnection() {
        try (Connection connection = getConnection()) {
            System.out.println("Подключение к базе данных установлено: " + url);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка подключения к базе данных", e);
        }
    }

    // Если таблиц ещё нет — создаём их и заливаем тестовые данные из schema.sql.
    // Благодаря этому методу проект запускается «из коробки»: достаточно создать пустую базу,
    // таблицы программа сделает сама.
    public static void initSchemaIfNeeded() {
        if (tableExists("donations")) {
            return; // Таблицы уже на месте — выходим и данные не трогаем.
        }

        System.out.println("Таблицы не найдены, создаю схему и тестовые данные...");
        String script = readResource("/schema.sql");

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            // Здесь Statement, а не PreparedStatement: выполняется целый SQL-скрипт
            // из наших же ресурсов, параметров в нём нет и пользовательский ввод сюда не попадает.
            statement.execute(script);
            System.out.println("База данных успешно инициализирована.");
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка выполнения schema.sql", e);
        }
    }

    // Есть ли в базе таблица с таким именем — спрашиваем у метаданных JDBC, а не у своего кода.
    private static boolean tableExists(String table) {
        try (Connection connection = getConnection();
             ResultSet rs = connection.getMetaData().getTables(null, "public", table, null)) {
            return rs.next(); // Нашлась хотя бы одна строка — значит таблица есть.
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка чтения метаданных базы данных", e);
        }
    }

    // Читает текстовый файл из ресурсов целиком в одну строку.
    private static String readResource(String name) {
        try (InputStream in = DatabaseManager.class.getResourceAsStream(name);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            // "\\A" — регулярное выражение «начало текста». Такой разделитель заставляет
            // Scanner отдать весь файл одним куском, а не по строкам.
            return scanner.useDelimiter("\\A").next();
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Не удалось прочитать файл " + name, e);
        }
    }

    // Пункт меню «Вывести таблицы базы данных».
    // Список таблиц и их столбцы берём не из своего кода, а прямо у базы через DatabaseMetaData —
    // то есть показываем реальную структуру БД, а не то, что мы про неё думаем.
    public static List<String> describeTables() {
        List<String> lines = new ArrayList<>();

        try (Connection connection = getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet tables = meta.getTables(null, "public", "%", new String[]{"TABLE"})) { // Внешний цикл идёт по таблицам схемы public.
                while (tables.next()) {
                    String table = tables.getString("TABLE_NAME");
                    lines.add("Таблица: " + table + " (строк: " + countRows(connection, table) + ")");

                    try (ResultSet columns = meta.getColumns(null, "public", table, "%")) { // Вложенный цикл идёт по столбцам текущей таблицы.
                        while (columns.next()) {
                            lines.add(String.format("    %-15s %s%s",
                                    columns.getString("COLUMN_NAME"),
                                    columns.getString("TYPE_NAME"),
                                    "NO".equals(columns.getString("IS_NULLABLE")) ? " NOT NULL" : "")); // IS_NULLABLE = "NO" значит, что столбец объявлен как NOT NULL.
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка чтения структуры базы данных", e);
        }
        return lines;
    }

    // Сколько строк в таблице.
    // Имя таблицы нельзя подставить через «?»: параметром может быть только значение,
    // но не название объекта БД. Склейка здесь безопасна, потому что имя пришло
    // из метаданных самой базы, а не от пользователя.
    private static int countRows(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
