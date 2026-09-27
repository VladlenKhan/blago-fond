package ru.mirea.fund.service; // Объявляем пакет класса.

import ru.mirea.fund.model.Donation; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationCategory; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationStatus; // Подключаем необходимый тип.

import java.math.BigDecimal; // Подключаем необходимый тип.
import java.math.RoundingMode; // Подключаем необходимый тип.
import java.util.ArrayList; // Подключаем необходимый тип.
import java.util.Comparator; // Подключаем необходимый тип.
import java.util.LinkedHashMap; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.
import java.util.Map; // Подключаем необходимый тип.
import java.util.stream.Collectors; // Подключаем необходимый тип.

/** Статистика фонда: показатели считаются на коллекциях через Stream API. */
public class StatisticsService { // Сервис статистики: считает показатели и отдаёт готовые строки.

    private final DonorService donorService = new DonorService(); // Сервис доноров: нужен для подсчёта их количества.
    private final DonationService donationService = new DonationService(); // Сервис пожертвований: источник данных для отчёта.

    /** Формирует готовые строки отчёта для вывода в меню. */
    public List<String> buildReport() { // Собирает отчёт строками, печатать будет меню.
        List<Donation> donations = donationService.findAll(); // Получаем данные из репозитория.
        int donorsCount = donorService.findAll().size(); // Считаем количество доноров.

        List<String> report = new ArrayList<>(); // Создаем переменную или объект.
        report.add("Всего доноров: " + donorsCount); // Добавляем элемент.
        report.add("Всего пожертвований: " + donations.size()); // Добавляем элемент.
        report.add("Общая сумма пожертвований: " + totalAmount(donations) + " руб."); // Добавляем элемент.
        report.add("Сумма завершённых пожертвований: " + completedAmount(donations) + " руб."); // Добавляем элемент.
        report.add("Средняя сумма пожертвования: " + averageAmount(donations) + " руб."); // Добавляем элемент.
        report.add("Максимальное пожертвование: " + maxAmount(donations) + " руб."); // Добавляем элемент.

        report.add(""); // Добавляем пустую строку.
        report.add("Количество по статусам:"); // Добавляем элемент.
        countByStatus(donations).forEach((status, count) -> // Перебираем элементы.
                report.add(String.format("    %-15s %d", status.getTitle(), count))); // Добавляем элемент.

        report.add(""); // Добавляем пустую строку.
        report.add("Сумма по направлениям:"); // Добавляем элемент.
        sumByCategory(donations).forEach((category, sum) -> // Перебираем элементы.
                report.add(String.format("    %-18s %s руб.", category.getTitle(), sum))); // Добавляем элемент.

        report.add(""); // Добавляем пустую строку.
        report.add("Топ-3 донора по сумме пожертвований:"); // Добавляем элемент.
        topDonors(donations, 3).forEach((name, sum) -> // Перебираем элементы.
                report.add(String.format("    %-25s %s руб.", name, sum))); // Добавляем элемент.

        return report; // Возвращаем результат.
    } // Завершаем блок.

    /** Общая сумма всех пожертвований. */
    private BigDecimal totalAmount(List<Donation> donations) { // Складывает суммы всех пожертвований.
        return donations.stream() // Создаем поток обработки данных.
                .map(Donation::getAmount) // Преобразуем элементы.
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем значения.
    } // Завершаем блок.

    /** Сумма завершённых пожертвований. */
    private BigDecimal completedAmount(List<Donation> donations) { // Складывает суммы только завершённых пожертвований.
        return donations.stream() // Создаем поток обработки данных.
                .filter(donation -> donation.getStatus() == DonationStatus.COMPLETED) // Фильтруем элементы.
                .map(Donation::getAmount) // Преобразуем элементы.
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем значения.
    } // Завершаем блок.

    /** Средняя сумма пожертвования. */
    private BigDecimal averageAmount(List<Donation> donations) { // Считает средний чек с округлением до копеек.
        if (donations.isEmpty()) { // Проверяем условие.
            return BigDecimal.ZERO; // Возвращаем результат.
        } // Завершаем блок.
        return totalAmount(donations).divide(BigDecimal.valueOf(donations.size()), 2, RoundingMode.HALF_UP); // Возвращаем результат.
    } // Завершаем блок.

    /** Максимальное пожертвование. */
    private BigDecimal maxAmount(List<Donation> donations) { // Находит самое крупное пожертвование.
        return donations.stream() // Создаем поток обработки данных.
                .map(Donation::getAmount) // Преобразуем элементы.
                .max(Comparator.naturalOrder()) // Находим максимум.
                .orElse(BigDecimal.ZERO); // Возвращаем ноль для пустого списка.
    } // Завершаем блок.

    /** Количество пожертвований по каждому статусу. */
    private Map<DonationStatus, Long> countByStatus(List<Donation> donations) { // Считает записи по каждому статусу, включая нулевые.
        Map<DonationStatus, Long> result = new LinkedHashMap<>(); // Создаем переменную или объект.
        for (DonationStatus status : DonationStatus.values()) { // Перебираем элементы.
            result.put(status, donations.stream().filter(d -> d.getStatus() == status).count()); // Добавляем запись в Map.
        } // Завершаем блок.
        return result; // Возвращаем результат.
    } // Завершаем блок.

    /** Сумма пожертвований по каждому направлению. */
    private Map<DonationCategory, BigDecimal> sumByCategory(List<Donation> donations) { // Считает сумму по каждому направлению помощи.
        Map<DonationCategory, BigDecimal> result = new LinkedHashMap<>(); // Создаем переменную или объект.
        for (DonationCategory category : DonationCategory.values()) { // Перебираем элементы.
            BigDecimal sum = donations.stream() // Создаем поток обработки данных.
                    .filter(d -> d.getCategory() == category) // Фильтруем элементы.
                    .map(Donation::getAmount) // Преобразуем элементы.
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // Суммируем значения.
            result.put(category, sum); // Добавляем запись в Map.
        } // Завершаем блок.
        return result; // Возвращаем результат.
    } // Завершаем блок.

    /** Топ доноров по сумме пожертвований. */
    private Map<String, BigDecimal> topDonors(List<Donation> donations, int limit) { // Находит самых крупных доноров по сумме.
        return donations.stream() // Создаем поток обработки данных.
                .collect(Collectors.groupingBy(Donation::getDonorName, // Группируем по имени донора.
                        Collectors.reducing(BigDecimal.ZERO, Donation::getAmount, BigDecimal::add))) // Суммируем значения группы.
                .entrySet().stream() // Создаем поток пар ключ-значение.
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()) // Сортируем элементы.
                .limit(limit) // Ограничиваем количество элементов.
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, // Собираем результат в коллекцию.
                        (a, b) -> a, // Разрешаем совпадение ключей.
                        LinkedHashMap::new)); // Сохраняем порядок сортировки.
    } // Завершаем блок.
} // Завершаем блок.
