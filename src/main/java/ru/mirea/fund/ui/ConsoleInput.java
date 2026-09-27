package ru.mirea.fund.ui; // Объявляем пакет класса.

import java.math.BigDecimal; // Подключаем необходимый тип.
import java.time.LocalDate; // Подключаем необходимый тип.
import java.time.format.DateTimeParseException; // Подключаем необходимый тип.
import java.util.Scanner; // Подключаем необходимый тип.

/** Безопасное чтение данных с консоли: программа не падает при некорректном вводе. */
public class ConsoleInput { // Помощник ввода: защищает программу от падения при опечатках.

    private final Scanner scanner = new Scanner(System.in); // Один Scanner на весь класс: поток System.in общий.

    /** Читает строку с клавиатуры. */
    public String readLine(String prompt) { // Печатает приглашение и читает строку без лишних пробелов.
        System.out.print(prompt); // Выводим результат в консоль.
        return scanner.nextLine().trim(); // Возвращаем результат.
    } // Завершаем блок.

    /** Читает целое число, повторяя запрос при ошибке. */
    public int readInt(String prompt) { // Требует целое число и повторяет вопрос при ошибке.
        while (true) { // Повторяем, пока условие истинно.
            String input = readLine(prompt); // Создаем переменную или объект.
            try { // Открываем блок обработки ошибок.
                return Integer.parseInt(input); // Преобразуем текст в число.
            } catch (NumberFormatException e) { // Обрабатываем исключение.
                System.out.println("Ошибка: нужно ввести целое число."); // Выводим результат в консоль.
            } // Завершаем блок.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Читает сумму денег, повторяя запрос при ошибке. */
    public BigDecimal readAmount(String prompt) { // Требует сумму и принимает запятую как разделитель.
        while (true) { // Повторяем, пока условие истинно.
            String input = readLine(prompt).replace(",", "."); // Заменяем запятую на точку.
            try { // Открываем блок обработки ошибок.
                return new BigDecimal(input); // Преобразуем текст в число.
            } catch (NumberFormatException e) { // Обрабатываем исключение.
                System.out.println("Ошибка: сумма должна быть числом, например 1500.50"); // Выводим результат в консоль.
            } // Завершаем блок.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Читает дату в формате ГГГГ-ММ-ДД, повторяя запрос при ошибке. */
    public LocalDate readDate(String prompt) { // Требует дату в формате ГГГГ-ММ-ДД.
        while (true) { // Повторяем, пока условие истинно.
            String input = readLine(prompt); // Создаем переменную или объект.
            try { // Открываем блок обработки ошибок.
                return LocalDate.parse(input); // Преобразуем текст в дату.
            } catch (DateTimeParseException e) { // Обрабатываем исключение.
                System.out.println("Ошибка: дата должна быть в формате ГГГГ-ММ-ДД, например 2025-03-15"); // Выводим результат в консоль.
            } // Завершаем блок.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Запрашивает подтверждение действия. */
    public boolean confirm(String prompt) { // Спрашивает подтверждение: всё кроме «да» — отказ.
        return readLine(prompt + " (да/нет): ").equalsIgnoreCase("да"); // Возвращаем результат.
    } // Завершаем блок.
} // Завершаем блок.
