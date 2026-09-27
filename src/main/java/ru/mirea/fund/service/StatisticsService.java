package ru.mirea.fund.service;

import ru.mirea.fund.model.Donation;
import ru.mirea.fund.model.DonationCategory;
import ru.mirea.fund.model.DonationStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Статистика фонда. Считается на коллекциях через Stream API. */
public class StatisticsService {

    private final DonorService donorService = new DonorService();
    private final DonationService donationService = new DonationService();

    /** Готовые строки отчёта — консольное меню их просто выводит. */
    public List<String> buildReport() {
        List<Donation> donations = donationService.findAll();
        int donorsCount = donorService.findAll().size();

        List<String> report = new ArrayList<>();
        report.add("Всего доноров: " + donorsCount);
        report.add("Всего пожертвований: " + donations.size());
        report.add("Общая сумма пожертвований: " + totalAmount(donations) + " руб.");
        report.add("Сумма завершённых пожертвований: " + completedAmount(donations) + " руб.");
        report.add("Средняя сумма пожертвования: " + averageAmount(donations) + " руб.");
        report.add("Максимальное пожертвование: " + maxAmount(donations) + " руб.");

        report.add("");
        report.add("Количество по статусам:");
        countByStatus(donations).forEach((status, count) ->
                report.add(String.format("    %-15s %d", status.getTitle(), count)));

        report.add("");
        report.add("Сумма по направлениям:");
        sumByCategory(donations).forEach((category, sum) ->
                report.add(String.format("    %-18s %s руб.", category.getTitle(), sum)));

        report.add("");
        report.add("Топ-3 донора по сумме пожертвований:");
        topDonors(donations, 3).forEach((name, sum) ->
                report.add(String.format("    %-25s %s руб.", name, sum)));

        return report;
    }

    private BigDecimal totalAmount(List<Donation> donations) {
        return donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal completedAmount(List<Donation> donations) {
        return donations.stream()
                .filter(donation -> donation.getStatus() == DonationStatus.COMPLETED)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal averageAmount(List<Donation> donations) {
        if (donations.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return totalAmount(donations).divide(BigDecimal.valueOf(donations.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal maxAmount(List<Donation> donations) {
        return donations.stream()
                .map(Donation::getAmount)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private Map<DonationStatus, Long> countByStatus(List<Donation> donations) {
        Map<DonationStatus, Long> result = new LinkedHashMap<>();
        for (DonationStatus status : DonationStatus.values()) {
            result.put(status, donations.stream().filter(d -> d.getStatus() == status).count());
        }
        return result;
    }

    private Map<DonationCategory, BigDecimal> sumByCategory(List<Donation> donations) {
        Map<DonationCategory, BigDecimal> result = new LinkedHashMap<>();
        for (DonationCategory category : DonationCategory.values()) {
            BigDecimal sum = donations.stream()
                    .filter(d -> d.getCategory() == category)
                    .map(Donation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.put(category, sum);
        }
        return result;
    }

    private Map<String, BigDecimal> topDonors(List<Donation> donations, int limit) {
        return donations.stream()
                .collect(Collectors.groupingBy(Donation::getDonorName,
                        Collectors.reducing(BigDecimal.ZERO, Donation::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }
}
