package ru.mirea.fund.util; // Объявляем пакет класса.

import ru.mirea.fund.model.Donation; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donor; // Подключаем необходимый тип.

import java.util.List; // Подключаем необходимый тип.

/** Интерфейс экспорта данных фонда: общий контракт для Excel и CSV. */
public interface Exporter { // Контракт экспорта: меню не знает, в какой формат пишем.

    String export(List<Donor> donors, List<Donation> donations); // Пишет данные в файл и возвращает путь к нему.

    String getFormatName(); // Возвращает название формата для меню.
} // Завершаем блок.
