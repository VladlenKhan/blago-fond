package ru.mirea.fund.util;

import ru.mirea.fund.model.Donation;
import ru.mirea.fund.model.Donor;

import java.util.List;

/**
 * Интерфейс экспорта данных фонда.
 * Реализации ExcelExporter и CsvExporter подставляются в меню через общий тип — это полиморфизм.
 */
public interface Exporter {

    /** Записывает доноров и пожертвования в файл и возвращает путь к созданному файлу. */
    String export(List<Donor> donors, List<Donation> donations);

    /** Название формата для меню. */
    String getFormatName();
}
