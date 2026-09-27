package ru.mirea.fund.ui; // Пакет пользовательского интерфейса: меню и чтение ввода.

import java.math.BigDecimal; // Точный тип для денег: в нём читаем суммы пожертвований.
import java.time.LocalDate; // Дата без времени: её вводит пользователь при поиске за период.
import java.time.format.DateTimeParseException; // Ошибка разбора даты: ловим её, если формат не тот.
import java.util.Scanner; // Читает то, что пользователь набирает в консоли.

// Безопасное чтение данных с консоли.
//
// Весь смысл класса — требование задания «программа не должна аварийно завершаться
// при ошибках ввода». Если бы мы писали scanner.nextInt() напрямую, то на слове "abc"
// программа упала бы с InputMismatchException. Здесь же ввод читается строкой
// и разбирается вручную, а при ошибке вопрос повторяется.
public class ConsoleInput {

    // Один Scanner на весь класс: создавать новый на каждый ввод нельзя,
    // они начнут конфликтовать за один и тот же поток System.in.
    private final Scanner scanner = new Scanner(System.in);

    // Читает строку: печатает приглашение и убирает случайные пробелы по краям.
    public String readLine(String prompt) {
        System.out.print(prompt); // print, а не println — курсор остаётся на той же строке, рядом с вопросом.
        return scanner.nextLine().trim();
    }

    // Читает целое число.
    // while(true) крутится до тех пор, пока ввод не окажется корректным: выход только через return.
    public int readInt(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return Integer.parseInt(input); // Удачно разобрали число — выходим из метода.
            } catch (NumberFormatException e) {
                // Ошибку не пробрасываем наверх: пользователь просто опечатался,
                // ему нужно подсказать и дать ввести заново.
                System.out.println("Ошибка: нужно ввести целое число.");
            }
        }
    }

    // Читает сумму денег.
    public BigDecimal readAmount(String prompt) {
        while (true) {
            String input = readLine(prompt).replace(",", "."); // У нас принято писать 1500,50, а BigDecimal понимает только 1500.50.
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: сумма должна быть числом, например 1500.50");
            }
        }
    }

    // Читает дату в формате ГГГГ-ММ-ДД.
    public LocalDate readDate(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: дата должна быть в формате ГГГГ-ММ-ДД, например 2025-03-15"); // В сообщении сразу даём пример правильного формата.
            }
        }
    }

    // Подтверждение опасного действия, например удаления. Всё, кроме «да», считается отказом.
    public boolean confirm(String prompt) {
        return readLine(prompt + " (да/нет): ").equalsIgnoreCase("да");
    }
}
