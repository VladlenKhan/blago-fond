package ru.mirea.fund.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.fund.model.Donation;
import ru.mirea.fund.model.Donor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/** Экспорт данных в файл Excel (.xlsx) с помощью Apache POI. Два листа: доноры и пожертвования. */
public class ExcelExporter implements Exporter {

    private static final String FILE_NAME = "export_fund.xlsx";

    @Override
    public String export(List<Donor> donors, List<Donation> donations) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(FILE_NAME)) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            writeDonors(workbook.createSheet("Доноры"), donors, headerStyle);
            writeDonations(workbook.createSheet("Пожертвования"), donations, headerStyle);
            workbook.write(out);

            return new java.io.File(FILE_NAME).getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать файл Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFormatName() {
        return "Excel (.xlsx)";
    }

    private void writeDonors(Sheet sheet, List<Donor> donors, CellStyle headerStyle) {
        writeHeader(sheet, headerStyle, "ID", "ФИО", "Email", "Телефон", "Город", "Дата регистрации");
        int rowNum = 1;
        for (Donor donor : donors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(donor.getId());
            row.createCell(1).setCellValue(donor.getFullName());
            row.createCell(2).setCellValue(donor.getEmail());
            row.createCell(3).setCellValue(donor.getPhone());
            row.createCell(4).setCellValue(donor.getCity());
            row.createCell(5).setCellValue(String.valueOf(donor.getRegisteredAt()));
        }
        autoSize(sheet, 6);
    }

    private void writeDonations(Sheet sheet, List<Donation> donations, CellStyle headerStyle) {
        writeHeader(sheet, headerStyle, "ID", "Донор", "Назначение", "Направление", "Статус", "Сумма", "Дата");
        int rowNum = 1;
        for (Donation donation : donations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(donation.getId());
            row.createCell(1).setCellValue(donation.getDonorName());
            row.createCell(2).setCellValue(donation.getPurpose());
            row.createCell(3).setCellValue(donation.getCategory().getTitle());
            row.createCell(4).setCellValue(donation.getStatus().getTitle());
            row.createCell(5).setCellValue(donation.getAmount().doubleValue());
            row.createCell(6).setCellValue(String.valueOf(donation.getCreatedAt().toLocalDate()));
        }
        autoSize(sheet, 7);
    }

    private void writeHeader(Sheet sheet, CellStyle style, String... titles) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(style);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
