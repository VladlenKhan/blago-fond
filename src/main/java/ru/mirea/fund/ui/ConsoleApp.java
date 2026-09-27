package ru.mirea.fund.ui; // Объявляем пакет класса.

import ru.mirea.fund.exception.BusinessException; // Подключаем необходимый тип.
import ru.mirea.fund.exception.DataAccessException; // Подключаем необходимый тип.
import ru.mirea.fund.exception.EntityNotFoundException; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donation; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationCategory; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationStatus; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donor; // Подключаем необходимый тип.
import ru.mirea.fund.service.DonationService; // Подключаем необходимый тип.
import ru.mirea.fund.service.DonorService; // Подключаем необходимый тип.
import ru.mirea.fund.service.StatisticsService; // Подключаем необходимый тип.
import ru.mirea.fund.util.CsvExporter; // Подключаем необходимый тип.
import ru.mirea.fund.util.DatabaseManager; // Подключаем необходимый тип.
import ru.mirea.fund.util.ExcelExporter; // Подключаем необходимый тип.
import ru.mirea.fund.util.Exporter; // Подключаем необходимый тип.

import java.math.BigDecimal; // Подключаем необходимый тип.
import java.time.LocalDate; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.

/** Консольное меню: выводит пункты и вызывает сервисы. SQL здесь отсутствует. */
public class ConsoleApp { // Консольное меню: собирает ввод и показывает результат.

    private static final String LINE = "========================================"; // Разделитель для оформления заголовка меню.

    private final ConsoleInput input = new ConsoleInput(); // Помощник безопасного ввода с клавиатуры.
    private final DonorService donorService = new DonorService(); // Сервис доноров: меню не знает про SQL.
    private final DonationService donationService = new DonationService(); // Сервис пожертвований со всеми бизнес-правилами.
    private final StatisticsService statisticsService = new StatisticsService(); // Сервис статистики для отдельного пункта меню.

    /** Главный цикл программы. */
    public void run() { // Крутит меню, пока пользователь не выберет выход.
        while (true) { // Повторяем, пока условие истинно.
            printMainMenu(); // Выводим главное меню.
            String choice = input.readLine("Выберите действие: "); // Создаем переменную или объект.

            try { // Открываем блок обработки ошибок.
                switch (choice) { // Выбираем сценарий выполнения.
                    case "1": donorsMenu(); break; // Обрабатываем вариант команды.
                    case "2": donationsMenu(); break; // Обрабатываем вариант команды.
                    case "3": searchMenu(); break; // Обрабатываем вариант команды.
                    case "4": filterMenu(); break; // Обрабатываем вариант команды.
                    case "5": showStatistics(); break; // Обрабатываем вариант команды.
                    case "6": exportMenu(); break; // Обрабатываем вариант команды.
                    case "7": showDatabaseTables(); break; // Обрабатываем вариант команды.
                    case "0": // Обрабатываем вариант команды.
                        System.out.println("Работа завершена."); // Выводим результат в консоль.
                        return; // Завершаем выполнение метода.
                    default: // Обрабатываем остальные варианты.
                        System.out.println("Нет такого пункта меню."); // Выводим результат в консоль.
                } // Завершаем блок.
            } catch (BusinessException | EntityNotFoundException e) { // Обрабатываем исключение.
                System.out.println("Ошибка: " + e.getMessage()); // Выводим результат в консоль.
            } catch (DataAccessException e) { // Обрабатываем исключение.
                System.out.println("Ошибка базы данных: " + e.getMessage()); // Выводим результат в консоль.
            } catch (RuntimeException e) { // Обрабатываем исключение.
                System.out.println("Непредвиденная ошибка: " + e.getMessage()); // Выводим результат в консоль.
            } // Завершаем блок.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Выводит главное меню. */
    private void printMainMenu() { // Печатает пункты главного меню.
        System.out.println(); // Выводим пустую строку.
        System.out.println(LINE); // Выводим результат в консоль.
        System.out.println("         БЛАГОТВОРИТЕЛЬНЫЙ ФОНД"); // Выводим результат в консоль.
        System.out.println(LINE); // Выводим результат в консоль.
        System.out.println("1. Доноры"); // Выводим результат в консоль.
        System.out.println("2. Пожертвования"); // Выводим результат в консоль.
        System.out.println("3. Поиск"); // Выводим результат в консоль.
        System.out.println("4. Фильтрация и сортировка"); // Выводим результат в консоль.
        System.out.println("5. Статистика"); // Выводим результат в консоль.
        System.out.println("6. Экспорт данных"); // Выводим результат в консоль.
        System.out.println("7. Вывести таблицы базы данных"); // Выводим результат в консоль.
        System.out.println("0. Выход"); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Подменю работы с донорами. */
    private void donorsMenu() { // Подменю доноров: CRUD и список по алфавиту.
        while (true) { // Повторяем, пока условие истинно.
            System.out.println(); // Выводим пустую строку.
            System.out.println("--- ДОНОРЫ ---"); // Выводим результат в консоль.
            System.out.println("1. Список всех доноров"); // Выводим результат в консоль.
            System.out.println("2. Найти донора по ID"); // Выводим результат в консоль.
            System.out.println("3. Добавить донора"); // Выводим результат в консоль.
            System.out.println("4. Изменить донора"); // Выводим результат в консоль.
            System.out.println("5. Удалить донора"); // Выводим результат в консоль.
            System.out.println("6. Список доноров по алфавиту"); // Выводим результат в консоль.
            System.out.println("0. Назад"); // Выводим результат в консоль.

            String choice = input.readLine("Выберите действие: "); // Создаем переменную или объект.
            try { // Открываем блок обработки ошибок.
                switch (choice) { // Выбираем сценарий выполнения.
                    case "1": printDonors(donorService.findAll()); break; // Обрабатываем вариант команды.
                    case "2": System.out.println(donorService.findById(input.readInt("Введите ID: "))); break; // Обрабатываем вариант команды.
                    case "3": createDonor(); break; // Обрабатываем вариант команды.
                    case "4": updateDonor(); break; // Обрабатываем вариант команды.
                    case "5": deleteDonor(); break; // Обрабатываем вариант команды.
                    case "6": printDonors(donorService.sortedByName()); break; // Обрабатываем вариант команды.
                    case "0": return; // Завершаем выполнение метода.
                    default: System.out.println("Нет такого пункта меню."); // Обрабатываем остальные варианты.
                } // Завершаем блок.
            } catch (BusinessException | EntityNotFoundException e) { // Обрабатываем исключение.
                System.out.println("Ошибка: " + e.getMessage()); // Выводим результат в консоль.
            } catch (DataAccessException e) { // Обрабатываем исключение.
                System.out.println("Ошибка базы данных: " + e.getMessage()); // Выводим результат в консоль.
            } // Завершаем блок.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Добавление нового донора. */
    private void createDonor() { // Собирает данные формы и передаёт их сервису.
        Donor donor = donorService.create( // Создаем переменную или объект.
                input.readLine("ФИО: "), // Читаем данные пользователя.
                input.readLine("Email: "), // Читаем данные пользователя.
                input.readLine("Телефон: "), // Читаем данные пользователя.
                input.readLine("Город: ")); // Читаем данные пользователя.
        System.out.println("Донор добавлен: " + donor); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Изменение данных донора. */
    private void updateDonor() { // Показывает текущие данные и сохраняет новые.
        int id = input.readInt("Введите ID донора: "); // Создаем переменную или объект.
        Donor donor = donorService.findById(id); // Получаем данные из репозитория.
        System.out.println("Текущие данные: " + donor); // Выводим результат в консоль.

        donorService.update(id, // Сохраняем данные.
                input.readLine("Новое ФИО: "), // Читаем данные пользователя.
                input.readLine("Новый email: "), // Читаем данные пользователя.
                input.readLine("Новый телефон: "), // Читаем данные пользователя.
                input.readLine("Новый город: ")); // Читаем данные пользователя.
        System.out.println("Данные донора обновлены."); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Удаление донора с подтверждением. */
    private void deleteDonor() { // Удаляет донора только после подтверждения.
        int id = input.readInt("Введите ID донора: "); // Создаем переменную или объект.
        Donor donor = donorService.findById(id); // Получаем данные из репозитория.

        System.out.println("Будет удалён: " + donor); // Выводим результат в консоль.
        System.out.println("Внимание: вместе с донором удалятся все его пожертвования."); // Выводим результат в консоль.

        if (input.confirm("Удалить донора?")) { // Проверяем условие.
            donorService.delete(id); // Удаляем элемент или запись.
            System.out.println("Донор удалён."); // Выводим результат в консоль.
        } else { // Выполняем альтернативную ветвь.
            System.out.println("Удаление отменено."); // Выводим результат в консоль.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Подменю работы с пожертвованиями. */
    private void donationsMenu() { // Подменю пожертвований: CRUD и смена статуса.
        while (true) { // Повторяем, пока условие истинно.
            System.out.println(); // Выводим пустую строку.
            System.out.println("--- ПОЖЕРТВОВАНИЯ ---"); // Выводим результат в консоль.
            System.out.println("1. Список всех пожертвований"); // Выводим результат в консоль.
            System.out.println("2. Найти пожертвование по ID"); // Выводим результат в консоль.
            System.out.println("3. Добавить пожертвование"); // Выводим результат в консоль.
            System.out.println("4. Изменить пожертвование"); // Выводим результат в консоль.
            System.out.println("5. Изменить статус пожертвования"); // Выводим результат в консоль.
            System.out.println("6. Удалить пожертвование"); // Выводим результат в консоль.
            System.out.println("0. Назад"); // Выводим результат в консоль.

            String choice = input.readLine("Выберите действие: "); // Создаем переменную или объект.
            try { // Открываем блок обработки ошибок.
                switch (choice) { // Выбираем сценарий выполнения.
                    case "1": printDonations(donationService.findAll()); break; // Обрабатываем вариант команды.
                    case "2": System.out.println(donationService.findById(input.readInt("Введите ID: "))); break; // Обрабатываем вариант команды.
                    case "3": createDonation(); break; // Обрабатываем вариант команды.
                    case "4": updateDonation(); break; // Обрабатываем вариант команды.
                    case "5": changeStatus(); break; // Обрабатываем вариант команды.
                    case "6": deleteDonation(); break; // Обрабатываем вариант команды.
                    case "0": return; // Завершаем выполнение метода.
                    default: System.out.println("Нет такого пункта меню."); // Обрабатываем остальные варианты.
                } // Завершаем блок.
            } catch (BusinessException | EntityNotFoundException e) { // Обрабатываем исключение.
                System.out.println("Ошибка: " + e.getMessage()); // Выводим результат в консоль.
            } catch (DataAccessException e) { // Обрабатываем исключение.
                System.out.println("Ошибка базы данных: " + e.getMessage()); // Выводим результат в консоль.
            } // Завершаем блок.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Создание нового пожертвования. */
    private void createDonation() { // Показывает доноров и создаёт пожертвование.
        printDonors(donorService.findAll()); // Показываем список доноров.

        Donation donation = donationService.create( // Создаем переменную или объект.
                input.readInt("ID донора: "), // Читаем данные пользователя.
                input.readLine("Назначение пожертвования: "), // Читаем данные пользователя.
                chooseCategory(), // Выбираем направление помощи.
                input.readAmount("Сумма, руб.: ")); // Читаем данные пользователя.
        System.out.println("Пожертвование создано: " + donation); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Изменение пожертвования. */
    private void updateDonation() { // Показывает текущие данные и сохраняет новые.
        int id = input.readInt("Введите ID пожертвования: "); // Создаем переменную или объект.
        System.out.println("Текущие данные: " + donationService.findById(id)); // Выводим результат в консоль.

        donationService.update(id, // Сохраняем данные.
                input.readLine("Новое назначение: "), // Читаем данные пользователя.
                chooseCategory(), // Выбираем направление помощи.
                input.readAmount("Новая сумма, руб.: ")); // Читаем данные пользователя.
        System.out.println("Пожертвование обновлено."); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Смена статуса пожертвования. */
    private void changeStatus() { // Переводит пожертвование в новый статус.
        int id = input.readInt("Введите ID пожертвования: "); // Создаем переменную или объект.
        Donation donation = donationService.findById(id); // Получаем данные из репозитория.
        System.out.println("Текущий статус: " + donation.getStatus()); // Выводим результат в консоль.

        donationService.changeStatus(id, chooseStatus()); // Сохраняем данные.
        System.out.println("Статус изменён."); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Удаление пожертвования с подтверждением. */
    private void deleteDonation() { // Удаляет пожертвование только после подтверждения.
        int id = input.readInt("Введите ID пожертвования: "); // Создаем переменную или объект.
        System.out.println("Будет удалено: " + donationService.findById(id)); // Выводим результат в консоль.

        if (input.confirm("Удалить пожертвование?")) { // Проверяем условие.
            donationService.delete(id); // Удаляем элемент или запись.
            System.out.println("Пожертвование удалено."); // Выводим результат в консоль.
        } else { // Выполняем альтернативную ветвь.
            System.out.println("Удаление отменено."); // Выводим результат в консоль.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Меню поиска. */
    private void searchMenu() { // Подменю поиска: четыре способа найти записи.
        System.out.println(); // Выводим пустую строку.
        System.out.println("--- ПОИСК ---"); // Выводим результат в консоль.
        System.out.println("1. Пожертвования по назначению"); // Выводим результат в консоль.
        System.out.println("2. Пожертвования по имени донора"); // Выводим результат в консоль.
        System.out.println("3. Пожертвования за период"); // Выводим результат в консоль.
        System.out.println("4. Доноры по имени или email"); // Выводим результат в консоль.
        System.out.println("0. Назад"); // Выводим результат в консоль.

        switch (input.readLine("Выберите действие: ")) { // Выбираем сценарий выполнения.
            case "1": // Обрабатываем вариант команды.
                printDonations(donationService.searchByPurpose(input.readLine("Введите часть назначения: "))); // Выполняем поиск.
                break; // Выходим из switch.
            case "2": // Обрабатываем вариант команды.
                printDonations(donationService.searchByDonorName(input.readLine("Введите имя донора: "))); // Выполняем поиск.
                break; // Выходим из switch.
            case "3": // Обрабатываем вариант команды.
                LocalDate from = input.readDate("Дата с (ГГГГ-ММ-ДД): "); // Читаем данные пользователя.
                LocalDate to = input.readDate("Дата по (ГГГГ-ММ-ДД): "); // Читаем данные пользователя.
                printDonations(donationService.searchByDateRange(from, to)); // Выполняем поиск.
                break; // Выходим из switch.
            case "4": // Обрабатываем вариант команды.
                printDonors(donorService.search(input.readLine("Введите имя или email: "))); // Выполняем поиск.
                break; // Выходим из switch.
            case "0": // Обрабатываем вариант команды.
                break; // Выходим из switch.
            default: // Обрабатываем остальные варианты.
                System.out.println("Нет такого пункта меню."); // Выводим результат в консоль.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Меню фильтрации и сортировки. */
    private void filterMenu() { // Подменю фильтров и сортировок.
        System.out.println(); // Выводим пустую строку.
        System.out.println("--- ФИЛЬТРАЦИЯ И СОРТИРОВКА ---"); // Выводим результат в консоль.
        System.out.println("1. Фильтр по статусу"); // Выводим результат в консоль.
        System.out.println("2. Фильтр по направлению помощи"); // Выводим результат в консоль.
        System.out.println("3. Фильтр по диапазону сумм"); // Выводим результат в консоль.
        System.out.println("4. Фильтр по донору"); // Выводим результат в консоль.
        System.out.println("5. Фильтр по городу донора"); // Выводим результат в консоль.
        System.out.println("6. Сортировка по сумме (по убыванию)"); // Выводим результат в консоль.
        System.out.println("7. Сортировка по дате (по возрастанию)"); // Выводим результат в консоль.
        System.out.println("8. Сортировка по имени донора"); // Выводим результат в консоль.
        System.out.println("0. Назад"); // Выводим результат в консоль.

        switch (input.readLine("Выберите действие: ")) { // Выбираем сценарий выполнения.
            case "1": // Обрабатываем вариант команды.
                printDonations(donationService.filterByStatus(chooseStatus())); // Фильтруем элементы.
                break; // Выходим из switch.
            case "2": // Обрабатываем вариант команды.
                printDonations(donationService.filterByCategory(chooseCategory())); // Фильтруем элементы.
                break; // Выходим из switch.
            case "3": // Обрабатываем вариант команды.
                BigDecimal from = input.readAmount("Сумма от: "); // Читаем данные пользователя.
                BigDecimal to = input.readAmount("Сумма до: "); // Читаем данные пользователя.
                printDonations(donationService.filterByAmountRange(from, to)); // Фильтруем элементы.
                break; // Выходим из switch.
            case "4": // Обрабатываем вариант команды.
                printDonations(donationService.filterByDonor(input.readInt("ID донора: "))); // Фильтруем элементы.
                break; // Выходим из switch.
            case "5": // Обрабатываем вариант команды.
                printDonors(donorService.filterByCity(input.readLine("Город: "))); // Фильтруем элементы.
                break; // Выходим из switch.
            case "6": // Обрабатываем вариант команды.
                printDonations(donationService.sortedByAmount(true)); // Сортируем элементы.
                break; // Выходим из switch.
            case "7": // Обрабатываем вариант команды.
                printDonations(donationService.sortedByDate()); // Сортируем элементы.
                break; // Выходим из switch.
            case "8": // Обрабатываем вариант команды.
                printDonations(donationService.sortedByDonorName()); // Сортируем элементы.
                break; // Выходим из switch.
            case "0": // Обрабатываем вариант команды.
                break; // Выходим из switch.
            default: // Обрабатываем остальные варианты.
                System.out.println("Нет такого пункта меню."); // Выводим результат в консоль.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Вывод статистики фонда. */
    private void showStatistics() { // Печатает готовый отчёт, полученный от сервиса.
        System.out.println(); // Выводим пустую строку.
        System.out.println("--- СТАТИСТИКА ФОНДА ---"); // Выводим результат в консоль.
        statisticsService.buildReport().forEach(System.out::println); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Экспорт данных: выбор формата через общий интерфейс Exporter. */
    private void exportMenu() { // Выбирает реализацию Exporter и запускает выгрузку.
        System.out.println(); // Выводим пустую строку.
        System.out.println("--- ЭКСПОРТ ДАННЫХ ---"); // Выводим результат в консоль.
        System.out.println("1. Excel (.xlsx)"); // Выводим результат в консоль.
        System.out.println("2. CSV (.csv)"); // Выводим результат в консоль.
        System.out.println("0. Назад"); // Выводим результат в консоль.

        String choice = input.readLine("Выберите формат: "); // Создаем переменную или объект.
        if (choice.equals("0")) { // Проверяем условие.
            return; // Завершаем выполнение метода.
        } // Завершаем блок.

        Exporter exporter; // Переменная типа интерфейса: за ней встанет Excel или CSV.
        if (choice.equals("1")) { // Проверяем условие.
            exporter = new ExcelExporter(); // Создаем переменную или объект.
        } else if (choice.equals("2")) { // Проверяем условие.
            exporter = new CsvExporter(); // Создаем переменную или объект.
        } else { // Выполняем альтернативную ветвь.
            System.out.println("Нет такого пункта меню."); // Выводим результат в консоль.
            return; // Завершаем выполнение метода.
        } // Завершаем блок.

        String path = exporter.export(donorService.findAll(), donationService.findAll()); // Выполняем экспорт данных.

        System.out.println("Данные экспортированы в формат " + exporter.getFormatName()); // Выводим результат в консоль.
        System.out.println("Файл: " + path); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Вывод структуры базы данных. */
    private void showDatabaseTables() { // Печатает таблицы и столбцы базы данных.
        System.out.println(); // Выводим пустую строку.
        System.out.println("--- ТАБЛИЦЫ БАЗЫ ДАННЫХ ---"); // Выводим результат в консоль.
        DatabaseManager.describeTables().forEach(System.out::println); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Печать списка доноров таблицей. */
    private void printDonors(List<Donor> donors) { // Выводит доноров таблицей; используется всеми пунктами меню.
        if (donors.isEmpty()) { // Проверяем условие.
            System.out.println("Ничего не найдено."); // Выводим результат в консоль.
            return; // Завершаем выполнение метода.
        } // Завершаем блок.

        System.out.println(); // Выводим пустую строку.
        System.out.printf("%-4s %-22s %-26s %-16s %-14s %s%n", "ID", "ФИО", "EMAIL", "ТЕЛЕФОН", "ГОРОД", "С"); // Выводим заголовки таблицы.

        for (Donor donor : donors) { // Перебираем элементы.
            System.out.printf("%-4d %-22s %-26s %-16s %-14s %s%n", donor.getId(), donor.getFullName(), // Выводим результат в консоль.
                    donor.getEmail(), donor.getPhone(), donor.getCity(), donor.getRegisteredAt()); // Подставляем значения полей.
        } // Завершаем блок.
        System.out.println("Всего записей: " + donors.size()); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Печать списка пожертвований таблицей. */
    private void printDonations(List<Donation> donations) { // Выводит пожертвования таблицей; используется всеми пунктами меню.
        if (donations.isEmpty()) { // Проверяем условие.
            System.out.println("Ничего не найдено."); // Выводим результат в консоль.
            return; // Завершаем выполнение метода.
        } // Завершаем блок.

        System.out.println(); // Выводим пустую строку.
        System.out.printf("%-4s %-30s %-16s %-14s %12s  %-20s %s%n", // Выводим заголовки таблицы.
                "ID", "НАЗНАЧЕНИЕ", "НАПРАВЛЕНИЕ", "СТАТУС", "СУММА", "ДОНОР", "ДАТА"); // Перечисляем названия столбцов.

        for (Donation donation : donations) { // Перебираем элементы.
            System.out.printf("%-4d %-30s %-16s %-14s %12s  %-20s %s%n", // Выводим результат в консоль.
                    donation.getId(), donation.getPurpose(), donation.getCategory().getTitle(), // Подставляем значения полей.
                    donation.getStatus().getTitle(), donation.getAmount(), // Подставляем значения полей.
                    donation.getDonorName(), donation.getCreatedAt().toLocalDate()); // Подставляем значения полей.
        } // Завершаем блок.
        System.out.println("Всего записей: " + donations.size()); // Выводим результат в консоль.
    } // Завершаем блок.

    /** Показывает доступные статусы и читает выбор пользователя. */
    private DonationStatus chooseStatus() { // Показывает статусы из enum и читает выбор.
        System.out.println("Доступные статусы:"); // Выводим результат в консоль.
        for (DonationStatus status : DonationStatus.values()) { // Перебираем элементы.
            System.out.println("    " + status); // Выводим результат в консоль.
        } // Завершаем блок.
        return DonationStatus.parse(input.readLine("Введите статус: ")); // Возвращаем результат.
    } // Завершаем блок.

    /** Показывает доступные направления и читает выбор пользователя. */
    private DonationCategory chooseCategory() { // Показывает направления из enum и читает выбор.
        System.out.println("Доступные направления:"); // Выводим результат в консоль.
        for (DonationCategory category : DonationCategory.values()) { // Перебираем элементы.
            System.out.println("    " + category); // Выводим результат в консоль.
        } // Завершаем блок.
        return DonationCategory.parse(input.readLine("Введите направление: ")); // Возвращаем результат.
    } // Завершаем блок.
} // Завершаем блок.
