package ru.mirea.fund.ui; // Пакет пользовательского интерфейса: меню и чтение ввода.

import ru.mirea.fund.exception.BusinessException; // Ошибка бизнес-правила: ловим её и показываем текст пользователю.
import ru.mirea.fund.exception.DataAccessException; // Ошибка базы данных: ловим её отдельно, чтобы дать другое сообщение.
import ru.mirea.fund.exception.EntityNotFoundException; // Ошибка «записи с таким id нет»: тоже ловим в меню.
import ru.mirea.fund.model.Donation; // Класс-сущность: его объекты меню выводит таблицей.
import ru.mirea.fund.model.DonationCategory; // Enum направлений: меню показывает его константы как варианты выбора.
import ru.mirea.fund.model.DonationStatus; // Enum статусов: меню показывает его константы как варианты выбора.
import ru.mirea.fund.model.Donor; // Класс-сущность: его объекты меню выводит таблицей.
import ru.mirea.fund.service.DonationService; // Сервис пожертвований: вся логика вызывается через него.
import ru.mirea.fund.service.DonorService; // Сервис доноров: вся логика вызывается через него.
import ru.mirea.fund.service.StatisticsService; // Сервис статистики: отдаёт готовые строки отчёта.
import ru.mirea.fund.util.CsvExporter; // Реализация экспорта в CSV.
import ru.mirea.fund.util.DatabaseManager; // Нужен только для пункта «Вывести таблицы базы данных».
import ru.mirea.fund.util.ExcelExporter; // Реализация экспорта в Excel.
import ru.mirea.fund.util.Exporter; // Общий интерфейс экспорта: через него работает полиморфизм.

import java.math.BigDecimal; // Точный тип для денег: в нём читаем границы диапазона сумм.
import java.time.LocalDate; // Дата без времени: её вводит пользователь при поиске за период.
import java.util.List; // Тип списков, которые меню получает от сервисов и печатает.

// Верхний слой приложения — консольное меню.
//
// Задача этого класса ровно две: показать пункты меню и вывести результат.
// Здесь НЕТ ни одного SQL-запроса и ни одной бизнес-проверки — всё это в сервисах
// и репозиториях. Так требует многослойная архитектура:
// Console UI -> Service -> Repository -> PostgreSQL.
public class ConsoleApp {

    private static final String LINE = "========================================"; // Разделитель для рамки заголовка меню.

    private final ConsoleInput input = new ConsoleInput(); // Помощник ввода: не даёт программе упасть на опечатке пользователя.

    // Меню обращается ТОЛЬКО к сервисам, про репозитории и JDBC оно не знает.
    private final DonorService donorService = new DonorService();
    private final DonationService donationService = new DonationService();
    private final StatisticsService statisticsService = new StatisticsService();

    // Главный цикл программы.
    // while(true) показывает меню снова и снова после каждой операции.
    // Программа завершается только по пункту «0. Выход», где стоит return.
    public void run() {
        while (true) {
            printMainMenu();
            String choice = input.readLine("Выберите действие: "); // Выбор читаем строкой, а не числом: тогда любой мусор просто попадёт в default.

            try {
                switch (choice) {
                    case "1": donorsMenu(); break;
                    case "2": donationsMenu(); break;
                    case "3": searchMenu(); break;
                    case "4": filterMenu(); break;
                    case "5": showStatistics(); break;
                    case "6": exportMenu(); break;
                    case "7": showDatabaseTables(); break;
                    case "0":
                        System.out.println("Работа завершена.");
                        return; // Единственный выход из программы.
                    default:
                        System.out.println("Нет такого пункта меню.");
                }

            // Блок catch ниже — «страховочная сетка» всей программы. Любая ошибка из глубины
            // (сервис, репозиторий, JDBC) долетит сюда, превратится в понятную строку,
            // и цикл меню продолжится вместо аварийного завершения.
            } catch (BusinessException | EntityNotFoundException e) { // Два разных исключения ловим одним catch через «|»: реакция на них одинаковая.
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage()); // На самый крайний случай: главное — не дать программе завершиться.
            }
        }
    }

    // Печатает пункты главного меню.
    private void printMainMenu() {
        System.out.println();
        System.out.println(LINE);
        System.out.println("         БЛАГОТВОРИТЕЛЬНЫЙ ФОНД");
        System.out.println(LINE);
        System.out.println("1. Доноры");
        System.out.println("2. Пожертвования");
        System.out.println("3. Поиск");
        System.out.println("4. Фильтрация и сортировка");
        System.out.println("5. Статистика");
        System.out.println("6. Экспорт данных");
        System.out.println("7. Вывести таблицы базы данных");
        System.out.println("0. Выход");
    }

    // Подменю доноров.
    // Тоже в цикле, чтобы можно было сделать несколько операций подряд
    // и только потом вернуться в главное меню по «0».
    private void donorsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- ДОНОРЫ ---");
            System.out.println("1. Список всех доноров");
            System.out.println("2. Найти донора по ID");
            System.out.println("3. Добавить донора");
            System.out.println("4. Изменить донора");
            System.out.println("5. Удалить донора");
            System.out.println("6. Список доноров по алфавиту");
            System.out.println("0. Назад");

            String choice = input.readLine("Выберите действие: ");
            try {
                switch (choice) {
                    case "1": printDonors(donorService.findAll()); break;
                    case "2": System.out.println(donorService.findById(input.readInt("Введите ID: "))); break;
                    case "3": createDonor(); break;
                    case "4": updateDonor(); break;
                    case "5": deleteDonor(); break;
                    case "6": printDonors(donorService.sortedByName()); break;
                    case "0": return; // Возврат в главное меню.
                    default: System.out.println("Нет такого пункта меню.");
                }
            // Ошибку ловим здесь, а не в главном меню: иначе после неё пользователя
            // выбросило бы из подменю доноров обратно наверх.
            } catch (BusinessException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    // Добавление донора: меню только собирает ввод, все проверки делает сервис.
    private void createDonor() {
        Donor donor = donorService.create(
                input.readLine("ФИО: "),
                input.readLine("Email: "),
                input.readLine("Телефон: "),
                input.readLine("Город: "));
        System.out.println("Донор добавлен: " + donor);
    }

    // Изменение донора: сначала показываем текущие данные, потом просим новые.
    private void updateDonor() {
        int id = input.readInt("Введите ID донора: ");
        Donor donor = donorService.findById(id); // Если такого ID нет — сразу ошибка, и лишние вопросы не задаются.
        System.out.println("Текущие данные: " + donor);

        donorService.update(id,
                input.readLine("Новое ФИО: "),
                input.readLine("Новый email: "),
                input.readLine("Новый телефон: "),
                input.readLine("Новый город: "));
        System.out.println("Данные донора обновлены.");
    }

    // Удаление донора: показываем, что именно удаляем, и предупреждаем о каскаде.
    private void deleteDonor() {
        int id = input.readInt("Введите ID донора: ");
        Donor donor = donorService.findById(id);

        System.out.println("Будет удалён: " + donor);
        System.out.println("Внимание: вместе с донором удалятся все его пожертвования.");

        if (input.confirm("Удалить донора?")) {
            donorService.delete(id);
            System.out.println("Донор удалён.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    // Подменю пожертвований: CRUD плюс отдельный пункт смены статуса.
    private void donationsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- ПОЖЕРТВОВАНИЯ ---");
            System.out.println("1. Список всех пожертвований");
            System.out.println("2. Найти пожертвование по ID");
            System.out.println("3. Добавить пожертвование");
            System.out.println("4. Изменить пожертвование");
            System.out.println("5. Изменить статус пожертвования");
            System.out.println("6. Удалить пожертвование");
            System.out.println("0. Назад");

            String choice = input.readLine("Выберите действие: ");
            try {
                switch (choice) {
                    case "1": printDonations(donationService.findAll()); break;
                    case "2": System.out.println(donationService.findById(input.readInt("Введите ID: "))); break;
                    case "3": createDonation(); break;
                    case "4": updateDonation(); break;
                    case "5": changeStatus(); break;
                    case "6": deleteDonation(); break;
                    case "0": return;
                    default: System.out.println("Нет такого пункта меню.");
                }
            } catch (BusinessException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    // Создание пожертвования.
    private void createDonation() {
        printDonors(donorService.findAll()); // Сначала показываем доноров, чтобы пользователь видел, какие ID вообще существуют.

        Donation donation = donationService.create(
                input.readInt("ID донора: "),
                input.readLine("Назначение пожертвования: "),
                chooseCategory(),
                input.readAmount("Сумма, руб.: "));
        System.out.println("Пожертвование создано: " + donation);
    }

    // Изменение пожертвования: показываем текущие данные, затем просим новые.
    private void updateDonation() {
        int id = input.readInt("Введите ID пожертвования: ");
        System.out.println("Текущие данные: " + donationService.findById(id));

        donationService.update(id,
                input.readLine("Новое назначение: "),
                chooseCategory(),
                input.readAmount("Новая сумма, руб.: "));
        System.out.println("Пожертвование обновлено.");
    }

    // Смена статуса: допустим ли переход, решает сервис вместе с enum'ом.
    private void changeStatus() {
        int id = input.readInt("Введите ID пожертвования: ");
        Donation donation = donationService.findById(id);
        System.out.println("Текущий статус: " + donation.getStatus());

        donationService.changeStatus(id, chooseStatus());
        System.out.println("Статус изменён.");
    }

    // Удаление пожертвования с подтверждением.
    private void deleteDonation() {
        int id = input.readInt("Введите ID пожертвования: ");
        System.out.println("Будет удалено: " + donationService.findById(id));

        if (input.confirm("Удалить пожертвование?")) {
            donationService.delete(id);
            System.out.println("Пожертвование удалено.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    // Меню поиска.
    // В отличие от меню доноров здесь нет while: после одного поиска возвращаемся в главное меню.
    private void searchMenu() {
        System.out.println();
        System.out.println("--- ПОИСК ---");
        System.out.println("1. Пожертвования по назначению");
        System.out.println("2. Пожертвования по имени донора");
        System.out.println("3. Пожертвования за период");
        System.out.println("4. Доноры по имени или email");
        System.out.println("0. Назад");

        switch (input.readLine("Выберите действие: ")) {
            case "1":
                printDonations(donationService.searchByPurpose(input.readLine("Введите часть назначения: ")));
                break;
            case "2":
                printDonations(donationService.searchByDonorName(input.readLine("Введите имя донора: ")));
                break;
            case "3":
                // Две даты читаем в отдельные переменные: если читать их прямо в вызове метода,
                // порядок аргументов будет неочевиден.
                LocalDate from = input.readDate("Дата с (ГГГГ-ММ-ДД): ");
                LocalDate to = input.readDate("Дата по (ГГГГ-ММ-ДД): ");
                printDonations(donationService.searchByDateRange(from, to));
                break;
            case "4":
                printDonors(donorService.search(input.readLine("Введите имя или email: ")));
                break;
            case "0":
                break;
            default:
                System.out.println("Нет такого пункта меню.");
        }
    }

    // Меню фильтрации и сортировки: пять фильтров и три способа сортировки.
    private void filterMenu() {
        System.out.println();
        System.out.println("--- ФИЛЬТРАЦИЯ И СОРТИРОВКА ---");
        System.out.println("1. Фильтр по статусу");
        System.out.println("2. Фильтр по направлению помощи");
        System.out.println("3. Фильтр по диапазону сумм");
        System.out.println("4. Фильтр по донору");
        System.out.println("5. Фильтр по городу донора");
        System.out.println("6. Сортировка по сумме (по убыванию)");
        System.out.println("7. Сортировка по дате (по возрастанию)");
        System.out.println("8. Сортировка по имени донора");
        System.out.println("0. Назад");

        switch (input.readLine("Выберите действие: ")) {
            case "1":
                printDonations(donationService.filterByStatus(chooseStatus()));
                break;
            case "2":
                printDonations(donationService.filterByCategory(chooseCategory()));
                break;
            case "3":
                BigDecimal from = input.readAmount("Сумма от: ");
                BigDecimal to = input.readAmount("Сумма до: ");
                printDonations(donationService.filterByAmountRange(from, to));
                break;
            case "4":
                printDonations(donationService.filterByDonor(input.readInt("ID донора: ")));
                break;
            case "5":
                printDonors(donorService.filterByCity(input.readLine("Город: ")));
                break;
            case "6":
                printDonations(donationService.sortedByAmount(true)); // true — сначала крупные пожертвования.
                break;
            case "7":
                printDonations(donationService.sortedByDate());
                break;
            case "8":
                printDonations(donationService.sortedByDonorName());
                break;
            case "0":
                break;
            default:
                System.out.println("Нет такого пункта меню.");
        }
    }

    // Статистику считает сервис, меню только печатает готовые строки.
    private void showStatistics() {
        System.out.println();
        System.out.println("--- СТАТИСТИКА ФОНДА ---");
        statisticsService.buildReport().forEach(System.out::println); // forEach со ссылкой на метод: то же, что for (String s : list) println(s).
    }

    // Экспорт данных — здесь виден ПОЛИМОРФИЗМ в чистом виде.
    private void exportMenu() {
        System.out.println();
        System.out.println("--- ЭКСПОРТ ДАННЫХ ---");
        System.out.println("1. Excel (.xlsx)");
        System.out.println("2. CSV (.csv)");
        System.out.println("0. Назад");

        String choice = input.readLine("Выберите формат: ");
        if (choice.equals("0")) {
            return;
        }

        Exporter exporter; // Переменная объявлена типом ИНТЕРФЕЙСА, а конкретный класс выбирается во время работы программы.
        if (choice.equals("1")) {
            exporter = new ExcelExporter();
        } else if (choice.equals("2")) {
            exporter = new CsvExporter();
        } else {
            System.out.println("Нет такого пункта меню.");
            return;
        }

        // Вызов один и тот же, а выполняется разный код — в зависимости от того,
        // какой объект лежит в переменной exporter.
        String path = exporter.export(donorService.findAll(), donationService.findAll());

        System.out.println("Данные экспортированы в формат " + exporter.getFormatName());
        System.out.println("Файл: " + path);
    }

    // Структура базы данных: таблицы, столбцы и количество строк.
    private void showDatabaseTables() {
        System.out.println();
        System.out.println("--- ТАБЛИЦЫ БАЗЫ ДАННЫХ ---");
        DatabaseManager.describeTables().forEach(System.out::println);
    }

    // Печать списка доноров таблицей.
    // Вынесено в отдельный метод, потому что список выводится из многих пунктов меню
    // (все записи, поиск, фильтр, сортировка) — дублировать этот код незачем.
    private void printDonors(List<Donor> donors) {
        if (donors.isEmpty()) {
            System.out.println("Ничего не найдено."); // Пустой список — это не ошибка, просто ничего не подошло.
            return;
        }

        System.out.println();
        // %-22s — выравнивание по левому краю в колонке шириной 22 символа,
        // благодаря этому столбцы таблицы не разъезжаются.
        System.out.printf("%-4s %-22s %-26s %-16s %-14s %s%n", "ID", "ФИО", "EMAIL", "ТЕЛЕФОН", "ГОРОД", "С");

        for (Donor donor : donors) {
            System.out.printf("%-4d %-22s %-26s %-16s %-14s %s%n", donor.getId(), donor.getFullName(),
                    donor.getEmail(), donor.getPhone(), donor.getCity(), donor.getRegisteredAt());
        }
        System.out.println("Всего записей: " + donors.size());
    }

    // Печать списка пожертвований таблицей — по той же схеме, что и список доноров.
    private void printDonations(List<Donation> donations) {
        if (donations.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }

        System.out.println();
        System.out.printf("%-4s %-30s %-16s %-14s %12s  %-20s %s%n",
                "ID", "НАЗНАЧЕНИЕ", "НАПРАВЛЕНИЕ", "СТАТУС", "СУММА", "ДОНОР", "ДАТА");

        for (Donation donation : donations) {
            System.out.printf("%-4d %-30s %-16s %-14s %12s  %-20s %s%n",
                    donation.getId(), donation.getPurpose(), donation.getCategory().getTitle(),
                    donation.getStatus().getTitle(), donation.getAmount(),
                    donation.getDonorName(), donation.getCreatedAt().toLocalDate());
        }
        System.out.println("Всего записей: " + donations.size());
    }

    // Показывает доступные статусы и читает выбор пользователя.
    // Список берём из values() самого enum'а: если добавится новый статус,
    // меню обновится само и править здесь ничего не придётся.
    private DonationStatus chooseStatus() {
        System.out.println("Доступные статусы:");
        for (DonationStatus status : DonationStatus.values()) {
            System.out.println("    " + status); // Сработает toString() перечисления и покажет код вместе с названием.
        }
        return DonationStatus.parse(input.readLine("Введите статус: ")); // parse() кинет BusinessException, если введено что-то не то.
    }

    // То же самое для направлений помощи.
    private DonationCategory chooseCategory() {
        System.out.println("Доступные направления:");
        for (DonationCategory category : DonationCategory.values()) {
            System.out.println("    " + category);
        }
        return DonationCategory.parse(input.readLine("Введите направление: "));
    }
}
