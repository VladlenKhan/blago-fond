package ru.mirea.fund.util; // Объявляем пакет класса.

import org.apache.poi.ss.usermodel.Cell; // Подключаем необходимый тип.
import org.apache.poi.ss.usermodel.CellStyle; // Подключаем необходимый тип.
import org.apache.poi.ss.usermodel.Font; // Подключаем необходимый тип.
import org.apache.poi.ss.usermodel.Row; // Подключаем необходимый тип.
import org.apache.poi.ss.usermodel.Sheet; // Подключаем необходимый тип.
import org.apache.poi.ss.usermodel.Workbook; // Подключаем необходимый тип.
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donation; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donor; // Подключаем необходимый тип.

import java.io.File; // Подключаем необходимый тип.
import java.io.FileOutputStream; // Подключаем необходимый тип.
import java.io.IOException; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.

/** Экспорт данных в Excel (.xlsx) через Apache POI: листы «Доноры» и «Пожертвования». */
public class ExcelExporter implements Exporter { // Реализация экспорта в .xlsx через Apache POI.

    private static final String FILE_NAME = "export_fund.xlsx"; // Имя создаваемого файла Excel.

    @Override // Переопределяем метод.
    public String export(List<Donor> donors, List<Donation> donations) { // Создаёт книгу из двух листов и сохраняет файл.
        try (Workbook workbook = new XSSFWorkbook(); // Открываем ресурсы безопасно.
             FileOutputStream out = new FileOutputStream(FILE_NAME)) { // Открываем файл для записи.

            CellStyle headerStyle = createHeaderStyle(workbook); // Создаем переменную или объект.
            writeDonors(workbook.createSheet("Доноры"), donors, headerStyle); // Заполняем лист доноров.
            writeDonations(workbook.createSheet("Пожертвования"), donations, headerStyle); // Заполняем лист пожертвований.
            workbook.write(out); // Записываем данные в файл.

            return new File(FILE_NAME).getAbsolutePath(); // Возвращаем результат.
        } catch (IOException e) { // Обрабатываем исключение.
            throw new RuntimeException("Не удалось записать файл Excel: " + e.getMessage(), e); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public String getFormatName() { // Возвращает название формата для сообщения в меню.
        return "Excel (.xlsx)"; // Возвращаем результат.
    } // Завершаем блок.

    /** Заполняет лист с донорами. */
    private void writeDonors(Sheet sheet, List<Donor> donors, CellStyle headerStyle) { // Заполняет лист «Доноры» строками таблицы.
        writeHeader(sheet, headerStyle, "ID", "ФИО", "Email", "Телефон", "Город", "Дата регистрации"); // Записываем заголовки.

        int rowNum = 1; // Создаем переменную или объект.
        for (Donor donor : donors) { // Перебираем элементы.
            Row row = sheet.createRow(rowNum++); // Создаем строку таблицы.
            row.createCell(0).setCellValue(donor.getId()); // Записываем значение ячейки.
            row.createCell(1).setCellValue(donor.getFullName()); // Записываем значение ячейки.
            row.createCell(2).setCellValue(donor.getEmail()); // Записываем значение ячейки.
            row.createCell(3).setCellValue(donor.getPhone()); // Записываем значение ячейки.
            row.createCell(4).setCellValue(donor.getCity()); // Записываем значение ячейки.
            row.createCell(5).setCellValue(String.valueOf(donor.getRegisteredAt())); // Записываем значение ячейки.
        } // Завершаем блок.
        autoSize(sheet, 6); // Подгоняем ширину столбцов.
    } // Завершаем блок.

    /** Заполняет лист с пожертвованиями. */
    private void writeDonations(Sheet sheet, List<Donation> donations, CellStyle headerStyle) { // Заполняет лист «Пожертвования» строками таблицы.
        writeHeader(sheet, headerStyle, "ID", "Донор", "Назначение", "Направление", "Статус", "Сумма", "Дата"); // Записываем заголовки.

        int rowNum = 1; // Создаем переменную или объект.
        for (Donation donation : donations) { // Перебираем элементы.
            Row row = sheet.createRow(rowNum++); // Создаем строку таблицы.
            row.createCell(0).setCellValue(donation.getId()); // Записываем значение ячейки.
            row.createCell(1).setCellValue(donation.getDonorName()); // Записываем значение ячейки.
            row.createCell(2).setCellValue(donation.getPurpose()); // Записываем значение ячейки.
            row.createCell(3).setCellValue(donation.getCategory().getTitle()); // Записываем значение ячейки.
            row.createCell(4).setCellValue(donation.getStatus().getTitle()); // Записываем значение ячейки.
            row.createCell(5).setCellValue(donation.getAmount().doubleValue()); // Записываем сумму числом.
            row.createCell(6).setCellValue(String.valueOf(donation.getCreatedAt().toLocalDate())); // Записываем значение ячейки.
        } // Завершаем блок.
        autoSize(sheet, 7); // Подгоняем ширину столбцов.
    } // Завершаем блок.

    /** Записывает строку заголовков. */
    private void writeHeader(Sheet sheet, CellStyle style, String... titles) { // Пишет шапку таблицы: принимает любое число названий.
        Row header = sheet.createRow(0); // Создаем строку таблицы.
        for (int i = 0; i < titles.length; i++) { // Перебираем элементы.
            Cell cell = header.createCell(i); // Создаем ячейку таблицы.
            cell.setCellValue(titles[i]); // Записываем значение ячейки.
            cell.setCellStyle(style); // Применяем стиль заголовка.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Создаёт стиль заголовка с жирным шрифтом. */
    private CellStyle createHeaderStyle(Workbook workbook) { // Готовит стиль шапки с жирным шрифтом.
        CellStyle style = workbook.createCellStyle(); // Создаем переменную или объект.
        Font font = workbook.createFont(); // Создаем переменную или объект.
        font.setBold(true); // Делаем шрифт жирным.
        style.setFont(font); // Применяем шрифт к стилю.
        return style; // Возвращаем результат.
    } // Завершаем блок.

    /** Подгоняет ширину столбцов под содержимое. */
    private void autoSize(Sheet sheet, int columns) { // Расширяет столбцы, чтобы текст не обрезался.
        for (int i = 0; i < columns; i++) { // Перебираем элементы.
            sheet.autoSizeColumn(i); // Подгоняем ширину столбца.
        } // Завершаем блок.
    } // Завершаем блок.
} // Завершаем блок.
