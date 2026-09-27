package ru.mirea.fund.util; // Объявляем пакет класса.

import ru.mirea.fund.model.Donation; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donor; // Подключаем необходимый тип.

import java.io.File; // Подключаем необходимый тип.
import java.io.IOException; // Подключаем необходимый тип.
import java.io.PrintWriter; // Подключаем необходимый тип.
import java.nio.charset.StandardCharsets; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.

/** Экспорт данных в CSV — вторая реализация интерфейса Exporter. */
public class CsvExporter implements Exporter { // Реализация экспорта в .csv обычным текстом.

    private static final String FILE_NAME = "export_fund.csv"; // Имя создаваемого файла CSV.

    @Override // Переопределяем метод.
    public String export(List<Donor> donors, List<Donation> donations) { // Пишет два блока данных в текстовый файл.
        try (PrintWriter writer = new PrintWriter(FILE_NAME, StandardCharsets.UTF_8.name())) { // Открываем ресурсы безопасно.

            writer.println("ДОНОРЫ"); // Записываем заголовок блока.
            writer.println("ID;ФИО;Email;Телефон;Город;Дата регистрации"); // Записываем заголовки столбцов.
            for (Donor donor : donors) { // Перебираем элементы.
                writer.printf("%d;%s;%s;%s;%s;%s%n", donor.getId(), donor.getFullName(), // Записываем строку файла.
                        donor.getEmail(), donor.getPhone(), donor.getCity(), donor.getRegisteredAt()); // Подставляем значения полей.
            } // Завершаем блок.

            writer.println(); // Записываем пустую строку.
            writer.println("ПОЖЕРТВОВАНИЯ"); // Записываем заголовок блока.
            writer.println("ID;Донор;Назначение;Направление;Статус;Сумма;Дата"); // Записываем заголовки столбцов.
            for (Donation donation : donations) { // Перебираем элементы.
                writer.printf("%d;%s;%s;%s;%s;%s;%s%n", donation.getId(), donation.getDonorName(), // Записываем строку файла.
                        donation.getPurpose(), donation.getCategory().getTitle(), // Подставляем значения полей.
                        donation.getStatus().getTitle(), donation.getAmount(), // Подставляем значения полей.
                        donation.getCreatedAt().toLocalDate()); // Подставляем значения полей.
            } // Завершаем блок.

            return new File(FILE_NAME).getAbsolutePath(); // Возвращаем результат.
        } catch (IOException e) { // Обрабатываем исключение.
            throw new RuntimeException("Не удалось записать файл CSV: " + e.getMessage(), e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public String getFormatName() { // Возвращает название формата для сообщения в меню.
        return "CSV (.csv)"; // Возвращаем результат.
    } // Завершаем блок.
} // Завершаем блок.
