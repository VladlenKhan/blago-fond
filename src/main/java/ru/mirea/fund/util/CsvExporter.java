package ru.mirea.fund.util; // Пакет вспомогательных классов: подключение к базе и экспорт данных.

import ru.mirea.fund.model.Donation; // Класс-сущность: его объекты уходят во второй блок файла.
import ru.mirea.fund.model.Donor; // Класс-сущность: его объекты уходят в первый блок файла.

import java.io.File; // Нужен, чтобы получить абсолютный путь к созданному файлу.
import java.io.IOException; // Ошибка записи: например, нет прав на создание файла.
import java.io.PrintWriter; // Пишет текст построчно, умеет printf с подстановкой значений.
import java.nio.charset.StandardCharsets; // Набор стандартных кодировок: берём отсюда UTF-8.
import java.util.List; // Оба набора данных приходят в экспортёр списками.

// Экспорт в CSV — вторая реализация интерфейса Exporter.
// Именно ради неё и нужен интерфейс: меню вызывает один и тот же метод export(),
// а какой код выполнится — этот или из ExcelExporter — решается тем, какой объект создали.
// Это и есть полиморфизм.
// Библиотека здесь не нужна: CSV — обычный текстовый файл, где значения разделены символом.
public class CsvExporter implements Exporter {

    private static final String FILE_NAME = "export_fund.csv"; // Файл создаётся в папке, откуда запущена программа.

    // Пишет в один файл два блока: сначала доноров, потом пожертвования.
    @Override
    public String export(List<Donor> donors, List<Donation> donations) {
        // Кодировку указываем явно: без UTF-8 кириллица в файле превратится в мусор.
        // PrintWriter закроется автоматически — он объявлен в try-with-resources.
        try (PrintWriter writer = new PrintWriter(FILE_NAME, StandardCharsets.UTF_8.name())) {

            writer.println("ДОНОРЫ");
            writer.println("ID;ФИО;Email;Телефон;Город;Дата регистрации");
            for (Donor donor : donors) {
                // Разделитель — точка с запятой, а не запятая: так Excel с русскими
                // настройками сразу раскладывает файл по столбцам.
                writer.printf("%d;%s;%s;%s;%s;%s%n", donor.getId(), donor.getFullName(),
                        donor.getEmail(), donor.getPhone(), donor.getCity(), donor.getRegisteredAt());
            }

            writer.println(); // Пустая строка отделяет один блок данных от другого.
            writer.println("ПОЖЕРТВОВАНИЯ");
            writer.println("ID;Донор;Назначение;Направление;Статус;Сумма;Дата");
            for (Donation donation : donations) {
                // %n вместо \n — перенос строки в том виде, который принят в текущей операционной системе.
                writer.printf("%d;%s;%s;%s;%s;%s;%s%n", donation.getId(), donation.getDonorName(),
                        donation.getPurpose(), donation.getCategory().getTitle(),
                        donation.getStatus().getTitle(), donation.getAmount(),
                        donation.getCreatedAt().toLocalDate());
            }

            return new File(FILE_NAME).getAbsolutePath(); // Возвращаем абсолютный путь, чтобы пользователь знал, где искать файл.
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать файл CSV: " + e.getMessage(), e);
        }
    }

    // Название формата для сообщения в меню.
    @Override
    public String getFormatName() {
        return "CSV (.csv)";
    }
}
