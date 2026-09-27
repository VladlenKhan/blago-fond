package ru.mirea.fund.service; // Пакет бизнес-логики: здесь проверяются все правила фонда.

import ru.mirea.fund.model.Donation; // Класс-сущность: по списку его объектов считаются все показатели.
import ru.mirea.fund.model.DonationCategory; // Enum направлений: по нему строится разрез сумм по категориям.
import ru.mirea.fund.model.DonationStatus; // Enum статусов: по нему строится разрез количества по статусам.

import java.math.BigDecimal; // Точный тип для денег: все суммы отчёта считаются им.
import java.math.RoundingMode; // Правило округления: без него деление BigDecimal упадёт на бесконечной дроби.
import java.util.ArrayList; // Реализация списка, в который складываем готовые строки отчёта.
import java.util.Comparator; // Нужен методу max(), чтобы знать, как сравнивать суммы.
import java.util.LinkedHashMap; // Map, сохраняющая порядок добавления: обычный HashMap перемешал бы строки отчёта.
import java.util.List; // Тип возвращаемого отчёта — список строк.
import java.util.Map; // Пары «ключ-значение» для разрезов по статусам, направлениям и донорам.
import java.util.stream.Collectors; // Группировка и сборка результатов Stream API.

// Статистика фонда.
// Все показатели считаются в Java на коллекциях через Stream API — это наглядная
// демонстрация требования задания. Данные берём один раз через DonationService
// и дальше обрабатываем уже готовый список, не дёргая базу на каждый показатель.
public class StatisticsService {

    private final DonorService donorService = new DonorService(); // Нужен только для подсчёта общего количества доноров.
    private final DonationService donationService = new DonationService(); // Источник данных: отдаёт список всех пожертвований.

    // Собирает готовый отчёт строками.
    // Почему List<String>, а не вывод на экран прямо отсюда: сервис не должен ничего печатать.
    // Он считает и отдаёт результат, а печатает пусть меню. Тогда этот же отчёт можно будет,
    // например, записать в файл, ничего не переписывая.
    public List<String> buildReport() {
        List<Donation> donations = donationService.findAll(); // Один запрос к базе на весь отчёт.
        int donorsCount = donorService.findAll().size();

        List<String> report = new ArrayList<>();

        // Шесть основных числовых показателей.
        report.add("Всего доноров: " + donorsCount);
        report.add("Всего пожертвований: " + donations.size());
        report.add("Общая сумма пожертвований: " + totalAmount(donations) + " руб.");
        report.add("Сумма завершённых пожертвований: " + completedAmount(donations) + " руб.");
        report.add("Средняя сумма пожертвования: " + averageAmount(donations) + " руб.");
        report.add("Максимальное пожертвование: " + maxAmount(donations) + " руб.");

        // Разрез по статусам.
        report.add("");
        report.add("Количество по статусам:");
        countByStatus(donations).forEach((status, count) -> // forEach у Map отдаёт сразу пару «ключ-значение».
                report.add(String.format("    %-15s %d", status.getTitle(), count)));

        // Разрез по направлениям помощи.
        report.add("");
        report.add("Сумма по направлениям:");
        sumByCategory(donations).forEach((category, sum) ->
                report.add(String.format("    %-18s %s руб.", category.getTitle(), sum)));

        // Топ доноров.
        report.add("");
        report.add("Топ-3 донора по сумме пожертвований:");
        topDonors(donations, 3).forEach((name, sum) ->
                report.add(String.format("    %-25s %s руб.", name, sum)));

        return report;
    }

    // Общая сумма: map() превращает поток пожертвований в поток сумм,
    // reduce() складывает их в одно число, начиная отсчёт с нуля.
    private BigDecimal totalAmount(List<Donation> donations) {
        return donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // То же самое, но сначала оставляем только завершённые пожертвования —
    // это деньги, которые фонд действительно потратил на помощь.
    private BigDecimal completedAmount(List<Donation> donations) {
        return donations.stream()
                .filter(donation -> donation.getStatus() == DonationStatus.COMPLETED)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Средняя сумма пожертвования.
    private BigDecimal averageAmount(List<Donation> donations) {
        if (donations.isEmpty()) {
            return BigDecimal.ZERO; // Защита от деления на ноль: если записей нет, среднее считать не из чего.
        }
        // У BigDecimal при делении обязательно указывают точность и правило округления,
        // иначе на бесконечной дроби программа упадёт с ArithmeticException.
        return totalAmount(donations).divide(BigDecimal.valueOf(donations.size()), 2, RoundingMode.HALF_UP);
    }

    // Самое крупное пожертвование.
    private BigDecimal maxAmount(List<Donation> donations) {
        return donations.stream()
                .map(Donation::getAmount)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO); // max() возвращает Optional, поэтому задаём значение для пустого списка.
    }

    // Сколько пожертвований в каждом статусе.
    // Идём циклом по values() перечисления, а не группируем сам список, чтобы в отчёт
    // попали ВСЕ статусы — даже те, в которых сейчас ноль записей.
    // LinkedHashMap сохраняет порядок добавления, поэтому статусы выводятся в естественном
    // порядке NEW -> CANCELLED, а не вперемешку, как было бы у HashMap.
    private Map<DonationStatus, Long> countByStatus(List<Donation> donations) {
        Map<DonationStatus, Long> result = new LinkedHashMap<>();
        for (DonationStatus status : DonationStatus.values()) {
            result.put(status, donations.stream().filter(d -> d.getStatus() == status).count());
        }
        return result;
    }

    // Сумма пожертвований по каждому направлению — та же идея, что и в разрезе по статусам.
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

    // Топ доноров по сумме — самый «продвинутый» кусок статистики. Разбор по шагам:
    // 1) groupingBy группирует пожертвования по имени донора, а reducing внутри складывает суммы каждой группы;
    // 2) entrySet().stream() переводит Map в поток пар «имя — сумма»;
    // 3) sorted(...reversed()) сортирует пары по сумме по убыванию;
    // 4) limit(limit) оставляет только первые N доноров;
    // 5) toMap с LinkedHashMap::new собирает результат обратно в Map С СОХРАНЕНИЕМ порядка —
    //    обычный HashMap порядок бы потерял, и вся сортировка оказалась бы бессмысленной.
    private Map<String, BigDecimal> topDonors(List<Donation> donations, int limit) {
        return donations.stream()
                .collect(Collectors.groupingBy(Donation::getDonorName,
                        Collectors.reducing(BigDecimal.ZERO, Donation::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, // Что делать при совпадении ключей; здесь имена уникальны, но параметр обязателен.
                        LinkedHashMap::new));
    }
}
