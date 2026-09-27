package ru.mirea.fund.service;

import ru.mirea.fund.exception.BusinessException;
import ru.mirea.fund.exception.EntityNotFoundException;
import ru.mirea.fund.model.Donation;
import ru.mirea.fund.model.DonationCategory;
import ru.mirea.fund.model.DonationStatus;
import ru.mirea.fund.repository.DonationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Бизнес-логика пожертвований.
 * Бизнес-правила:
 * 1. Назначение обязательно (минимум 3 символа).
 * 2. Сумма строго больше нуля и не превышает 1 000 000 руб.
 * 3. Донор должен существовать в базе.
 * 4. Разрешены только переходы NEW -> CONFIRMED -> COMPLETED (отмена — до завершения).
 * 5. Завершённое пожертвование нельзя изменить или удалить.
 */
public class DonationService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");

    private final DonationRepository repository = new DonationRepository();
    private final DonorService donorService = new DonorService();

    public Donation create(int donorId, String purpose, DonationCategory category, BigDecimal amount) {
        donorService.findById(donorId);          // правило 3: проверка существования донора
        validate(purpose, amount);
        Donation donation = new Donation(donorId, purpose.trim(), category, amount);
        repository.save(donation);
        return findById(donation.getId());       // перечитываем, чтобы получить имя донора
    }

    public List<Donation> findAll() {
        return repository.findAll();
    }

    public Donation findById(int id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пожертвование", id));
    }

    public void update(int id, String purpose, DonationCategory category, BigDecimal amount) {
        Donation donation = findById(id);
        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new BusinessException("Завершённое пожертвование нельзя изменить");
        }
        validate(purpose, amount);
        donation.setPurpose(purpose.trim());
        donation.setCategory(category);
        donation.setAmount(amount);
        repository.update(donation);
    }

    /** Правило 4: переход статуса проверяет сам enum. */
    public void changeStatus(int id, DonationStatus newStatus) {
        Donation donation = findById(id);
        if (!donation.getStatus().canChangeTo(newStatus)) {
            throw new BusinessException("Недопустимый переход статуса: "
                    + donation.getStatus().getTitle() + " -> " + newStatus.getTitle());
        }
        donation.setStatus(newStatus);
        repository.update(donation);
    }

    public void delete(int id) {
        Donation donation = findById(id);
        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new BusinessException("Завершённое пожертвование нельзя удалить — оно входит в отчётность фонда");
        }
        repository.delete(id);
    }

    // ---------- Поиск (SQL-запросы с PreparedStatement) ----------

    public List<Donation> searchByPurpose(String text) {
        requireText(text);
        return repository.searchByPurpose(text.trim());
    }

    public List<Donation> searchByDonorName(String text) {
        requireText(text);
        return repository.searchByDonorName(text.trim());
    }

    public List<Donation> searchByDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("Дата начала не может быть позже даты окончания");
        }
        return repository.searchByDateRange(from, to);
    }

    // ---------- Фильтрация (Stream API) ----------

    public List<Donation> filterByStatus(DonationStatus status) {
        return repository.findAll().stream()
                .filter(donation -> donation.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Donation> filterByCategory(DonationCategory category) {
        return repository.findAll().stream()
                .filter(donation -> donation.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Donation> filterByAmountRange(BigDecimal from, BigDecimal to) {
        if (from.compareTo(to) > 0) {
            throw new BusinessException("Минимальная сумма не может быть больше максимальной");
        }
        return repository.findAll().stream()
                .filter(donation -> donation.getAmount().compareTo(from) >= 0
                        && donation.getAmount().compareTo(to) <= 0)
                .collect(Collectors.toList());
    }

    public List<Donation> filterByDonor(int donorId) {
        donorService.findById(donorId);
        return repository.findByDonorId(donorId);
    }

    // ---------- Сортировка (Comparator) ----------

    public List<Donation> sortedByAmount(boolean descending) {
        Comparator<Donation> byAmount = Comparator.comparing(Donation::getAmount);
        return repository.findAll().stream()
                .sorted(descending ? byAmount.reversed() : byAmount)
                .collect(Collectors.toList());
    }

    public List<Donation> sortedByDate() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Donation::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<Donation> sortedByDonorName() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Donation::getDonorName).thenComparing(Donation::getAmount))
                .collect(Collectors.toList());
    }

    private void validate(String purpose, BigDecimal amount) {
        if (purpose == null || purpose.trim().length() < 3) {
            throw new BusinessException("Назначение пожертвования обязательно (минимум 3 символа)");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Сумма пожертвования должна быть больше нуля");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessException("Сумма одного пожертвования не может превышать " + MAX_AMOUNT + " руб.");
        }
    }

    private void requireText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Строка поиска не может быть пустой");
        }
    }
}
