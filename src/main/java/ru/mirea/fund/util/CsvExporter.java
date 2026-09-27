package ru.mirea.fund.util;

import ru.mirea.fund.model.Donation;
import ru.mirea.fund.model.Donor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Экспорт данных в CSV — вторая реализация интерфейса Exporter. */
public class CsvExporter implements Exporter {

    private static final String FILE_NAME = "export_fund.csv";

    @Override
    public String export(List<Donor> donors, List<Donation> donations) {
        try (PrintWriter writer = new PrintWriter(FILE_NAME, StandardCharsets.UTF_8.name())) {
            writer.println("ДОНОРЫ");
            writer.println("ID;ФИО;Email;Телефон;Город;Дата регистрации");
            for (Donor donor : donors) {
                writer.printf("%d;%s;%s;%s;%s;%s%n", donor.getId(), donor.getFullName(),
                        donor.getEmail(), donor.getPhone(), donor.getCity(), donor.getRegisteredAt());
            }

            writer.println();
            writer.println("ПОЖЕРТВОВАНИЯ");
            writer.println("ID;Донор;Назначение;Направление;Статус;Сумма;Дата");
            for (Donation donation : donations) {
                writer.printf("%d;%s;%s;%s;%s;%s;%s%n", donation.getId(), donation.getDonorName(),
                        donation.getPurpose(), donation.getCategory().getTitle(),
                        donation.getStatus().getTitle(), donation.getAmount(),
                        donation.getCreatedAt().toLocalDate());
            }

            return new File(FILE_NAME).getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать файл CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFormatName() {
        return "CSV (.csv)";
    }
}
