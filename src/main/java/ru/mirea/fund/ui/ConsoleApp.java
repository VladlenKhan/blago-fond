package ru.mirea.fund.ui;

import ru.mirea.fund.exception.BusinessException;
import ru.mirea.fund.exception.DataAccessException;
import ru.mirea.fund.exception.EntityNotFoundException;
import ru.mirea.fund.model.Donation;
import ru.mirea.fund.model.DonationCategory;
import ru.mirea.fund.model.DonationStatus;
import ru.mirea.fund.model.Donor;
import ru.mirea.fund.service.DonationService;
import ru.mirea.fund.service.DonorService;
import ru.mirea.fund.service.StatisticsService;
import ru.mirea.fund.util.CsvExporter;
import ru.mirea.fund.util.DatabaseManager;
import ru.mirea.fund.util.ExcelExporter;
import ru.mirea.fund.util.Exporter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Консольное меню. Здесь нет SQL — только вызовы сервисов и вывод результата. */
public class ConsoleApp {

    private static final String LINE = "========================================";

    private final ConsoleInput input = new ConsoleInput();
    private final DonorService donorService = new DonorService();
    private final DonationService donationService = new DonationService();
    private final StatisticsService statisticsService = new StatisticsService();

    public void run() {
        while (true) {
            printMainMenu();
            String choice = input.readLine("Выберите действие: ");
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
                        return;
                    default:
                        System.out.println("Нет такого пункта меню.");
                }
            } catch (BusinessException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
    }

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

    // ---------------- Доноры ----------------

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

    private void createDonor() {
        Donor donor = donorService.create(
                input.readLine("ФИО: "),
                input.readLine("Email: "),
                input.readLine("Телефон: "),
                input.readLine("Город: "));
        System.out.println("Донор добавлен: " + donor);
    }

    private void updateDonor() {
        int id = input.readInt("Введите ID донора: ");
        Donor donor = donorService.findById(id);
        System.out.println("Текущие данные: " + donor);
        donorService.update(id,
                input.readLine("Новое ФИО: "),
                input.readLine("Новый email: "),
                input.readLine("Новый телефон: "),
                input.readLine("Новый город: "));
        System.out.println("Данные донора обновлены.");
    }

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

    // ---------------- Пожертвования ----------------

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

    private void createDonation() {
        printDonors(donorService.findAll());
        Donation donation = donationService.create(
                input.readInt("ID донора: "),
                input.readLine("Назначение пожертвования: "),
                chooseCategory(),
                input.readAmount("Сумма, руб.: "));
        System.out.println("Пожертвование создано: " + donation);
    }

    private void updateDonation() {
        int id = input.readInt("Введите ID пожертвования: ");
        System.out.println("Текущие данные: " + donationService.findById(id));
        donationService.update(id,
                input.readLine("Новое назначение: "),
                chooseCategory(),
                input.readAmount("Новая сумма, руб.: "));
        System.out.println("Пожертвование обновлено.");
    }

    private void changeStatus() {
        int id = input.readInt("Введите ID пожертвования: ");
        Donation donation = donationService.findById(id);
        System.out.println("Текущий статус: " + donation.getStatus());
        donationService.changeStatus(id, chooseStatus());
        System.out.println("Статус изменён.");
    }

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

    // ---------------- Поиск ----------------

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

    // ---------------- Фильтрация и сортировка ----------------

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
                printDonations(donationService.sortedByAmount(true));
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

    // ---------------- Статистика, экспорт, таблицы ----------------

    private void showStatistics() {
        System.out.println();
        System.out.println("--- СТАТИСТИКА ФОНДА ---");
        statisticsService.buildReport().forEach(System.out::println);
    }

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
        // полиморфизм: обе реализации используются через общий интерфейс Exporter
        Exporter exporter;
        if (choice.equals("1")) {
            exporter = new ExcelExporter();
        } else if (choice.equals("2")) {
            exporter = new CsvExporter();
        } else {
            System.out.println("Нет такого пункта меню.");
            return;
        }
        String path = exporter.export(donorService.findAll(), donationService.findAll());
        System.out.println("Данные экспортированы в формат " + exporter.getFormatName());
        System.out.println("Файл: " + path);
    }

    private void showDatabaseTables() {
        System.out.println();
        System.out.println("--- ТАБЛИЦЫ БАЗЫ ДАННЫХ ---");
        DatabaseManager.describeTables().forEach(System.out::println);
    }

    // ---------------- Вспомогательные методы вывода ----------------

    private void printDonors(List<Donor> donors) {
        if (donors.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }
        System.out.println();
        System.out.printf("%-4s %-22s %-26s %-16s %-14s %s%n", "ID", "ФИО", "EMAIL", "ТЕЛЕФОН", "ГОРОД", "С");
        for (Donor donor : donors) {
            System.out.printf("%-4d %-22s %-26s %-16s %-14s %s%n", donor.getId(), donor.getFullName(),
                    donor.getEmail(), donor.getPhone(), donor.getCity(), donor.getRegisteredAt());
        }
        System.out.println("Всего записей: " + donors.size());
    }

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

    private DonationStatus chooseStatus() {
        System.out.println("Доступные статусы:");
        for (DonationStatus status : DonationStatus.values()) {
            System.out.println("    " + status);
        }
        return DonationStatus.parse(input.readLine("Введите статус: "));
    }

    private DonationCategory chooseCategory() {
        System.out.println("Доступные направления:");
        for (DonationCategory category : DonationCategory.values()) {
            System.out.println("    " + category);
        }
        return DonationCategory.parse(input.readLine("Введите направление: "));
    }
}
