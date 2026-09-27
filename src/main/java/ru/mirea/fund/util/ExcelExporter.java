package ru.mirea.fund.util; // Пакет вспомогательных классов: подключение к базе и экспорт данных.

import org.apache.poi.ss.usermodel.Cell; // Ячейка таблицы: в неё пишется одно значение.
import org.apache.poi.ss.usermodel.CellStyle; // Оформление ячейки: им делаем шапку жирной.
import org.apache.poi.ss.usermodel.Font; // Шрифт: настраиваем жирное начертание для заголовков.
import org.apache.poi.ss.usermodel.Row; // Строка листа, состоит из ячеек.
import org.apache.poi.ss.usermodel.Sheet; // Лист книги: у нас их два — доноры и пожертвования.
import org.apache.poi.ss.usermodel.Workbook; // Книга Excel целиком, то есть весь файл.
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // Реализация книги для формата .xlsx (у старого .xls был бы HSSFWorkbook).
import ru.mirea.fund.model.Donation; // Класс-сущность: его объекты становятся строками второго листа.
import ru.mirea.fund.model.Donor; // Класс-сущность: его объекты становятся строками первого листа.

import java.io.File; // Нужен, чтобы получить абсолютный путь к созданному файлу.
import java.io.FileOutputStream; // Поток записи: через него книга сохраняется на диск.
import java.io.IOException; // Ошибка записи: файл может быть занят Excel или закрыт для записи.
import java.util.List; // Оба набора данных приходят в экспортёр списками.

// Экспорт данных в Excel (.xlsx) с помощью библиотеки Apache POI.
// Первая реализация интерфейса Exporter. Создаёт книгу из двух листов: «Доноры» и «Пожертвования».
// Термины POI: Workbook — книга (весь файл), Sheet — лист, Row — строка, Cell — ячейка.
public class ExcelExporter implements Exporter {

    private static final String FILE_NAME = "export_fund.xlsx"; // Файл создаётся в папке, откуда запущена программа.

    // Создаёт книгу, заполняет оба листа и сохраняет файл на диск.
    @Override
    public String export(List<Donor> donors, List<Donation> donations) {
        // Оба ресурса в try-with-resources: и книга, и файловый поток закроются сами.
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(FILE_NAME)) {

            CellStyle headerStyle = createHeaderStyle(workbook); // Стиль создаём один раз на всю книгу и переиспользуем.

            writeDonors(workbook.createSheet("Доноры"), donors, headerStyle);
            writeDonations(workbook.createSheet("Пожертвования"), donations, headerStyle);

            workbook.write(out); // Только в этот момент данные реально попадают в файл.

            return new File(FILE_NAME).getAbsolutePath(); // Возвращаем абсолютный путь, чтобы пользователь знал, где искать файл.
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать файл Excel: " + e.getMessage(), e);
        }
    }

    // Название формата для сообщения в меню.
    @Override
    public String getFormatName() {
        return "Excel (.xlsx)";
    }

    // Заполняет лист с донорами.
    private void writeDonors(Sheet sheet, List<Donor> donors, CellStyle headerStyle) {
        writeHeader(sheet, headerStyle, "ID", "ФИО", "Email", "Телефон", "Город", "Дата регистрации");

        int rowNum = 1; // Строка 0 занята шапкой, поэтому данные идут с первой.
        for (Donor donor : donors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(donor.getId()); // Нумерация ячеек, в отличие от строк отчёта, начинается с нуля.
            row.createCell(1).setCellValue(donor.getFullName());
            row.createCell(2).setCellValue(donor.getEmail());
            row.createCell(3).setCellValue(donor.getPhone());
            row.createCell(4).setCellValue(donor.getCity());
            row.createCell(5).setCellValue(String.valueOf(donor.getRegisteredAt()));
        }
        autoSize(sheet, 6);
    }

    // Заполняет лист с пожертвованиями.
    private void writeDonations(Sheet sheet, List<Donation> donations, CellStyle headerStyle) {
        writeHeader(sheet, headerStyle, "ID", "Донор", "Назначение", "Направление", "Статус", "Сумма", "Дата");

        int rowNum = 1;
        for (Donation donation : donations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(donation.getId());
            row.createCell(1).setCellValue(donation.getDonorName());
            row.createCell(2).setCellValue(donation.getPurpose());
            row.createCell(3).setCellValue(donation.getCategory().getTitle()); // getTitle() даёт понятное название вместо кода MEDICINE.
            row.createCell(4).setCellValue(donation.getStatus().getTitle());
            // Сумму пишем числом (doubleValue), а не текстом: иначе в Excel по ней
            // нельзя будет посчитать итог или построить диаграмму.
            row.createCell(5).setCellValue(donation.getAmount().doubleValue());
            row.createCell(6).setCellValue(String.valueOf(donation.getCreatedAt().toLocalDate()));
        }
        autoSize(sheet, 7);
    }

    // Пишет строку заголовков и делает её жирной.
    // String... titles — переменное число аргументов: можно передать сколько угодно
    // названий столбцов, внутри метода это обычный массив.
    private void writeHeader(Sheet sheet, CellStyle style, String... titles) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(style);
        }
    }

    // Создаёт стиль «жирный шрифт» для строки заголовков.
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    // Подгоняет ширину столбцов под содержимое, чтобы длинный текст не обрезался.
    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
