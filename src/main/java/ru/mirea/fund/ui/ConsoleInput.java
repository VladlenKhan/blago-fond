package ru.mirea.fund.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Безопасное чтение данных с консоли: программа не падает при некорректном вводе. */
public class ConsoleInput {

    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Повторяет запрос, пока пользователь не введёт целое число. */
    public int readInt(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести целое число.");
            }
        }
    }

    /** Повторяет запрос, пока пользователь не введёт корректную сумму. */
    public BigDecimal readAmount(String prompt) {
        while (true) {
            String input = readLine(prompt).replace(",", ".");
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: сумма должна быть числом, например 1500.50");
            }
        }
    }

    /** Повторяет запрос, пока пользователь не введёт дату в формате ГГГГ-ММ-ДД. */
    public LocalDate readDate(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: дата должна быть в формате ГГГГ-ММ-ДД, например 2025-03-15");
            }
        }
    }

    public boolean confirm(String prompt) {
        return readLine(prompt + " (да/нет): ").equalsIgnoreCase("да");
    }
}
