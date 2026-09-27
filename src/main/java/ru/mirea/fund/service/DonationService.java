package ru.mirea.fund.service; // Объявляем пакет класса.

import ru.mirea.fund.exception.BusinessException; // Подключаем необходимый тип.
import ru.mirea.fund.exception.EntityNotFoundException; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donation; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationCategory; // Подключаем необходимый тип.
import ru.mirea.fund.model.DonationStatus; // Подключаем необходимый тип.
import ru.mirea.fund.repository.DonationRepository; // Подключаем необходимый тип.

import java.math.BigDecimal; // Подключаем необходимый тип.
import java.time.LocalDate; // Подключаем необходимый тип.
import java.util.Comparator; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.
import java.util.stream.Collectors; // Подключаем необходимый тип.

/**
 * Бизнес-логика пожертвований.
 * Правила: назначение обязательно; сумма от 0 до 1 000 000; донор существует;
 * переходы NEW -> CONFIRMED -> COMPLETED; завершённое нельзя менять и удалять.
 */
public class DonationService { // Главный сервис: вся бизнес-логика пожертвований.

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000"); // Потолок одной суммы, чтобы число не терялось в коде.

    private final DonationRepository repository = new DonationRepository(); // Репозиторий пожертвований для работы с БД.
    private final DonorService donorService = new DonorService(); // Сервис доноров: переиспользуем проверку существования.

    public Donation create(int donorId, String purpose, DonationCategory category, BigDecimal amount) { // Создаёт пожертвование после всех проверок.
        donorService.findById(donorId); // Проверяем существование донора.
        validate(purpose, amount); // Проверяем бизнес-правила.

        Donation donation = new Donation(donorId, purpose.trim(), category, amount); // Создаем переменную или объект.
        repository.save(donation); // Сохраняем данные.
        return findById(donation.getId()); // Возвращаем результат.
    } // Завершаем блок.

    public List<Donation> findAll() { // Возвращает список всех пожертвований.
        return repository.findAll(); // Возвращаем результат.
    } // Завершаем блок.

    public Donation findById(int id) { // Находит пожертвование или сообщает, что его нет.
        return repository.findById(id) // Получаем данные из репозитория.
                .orElseThrow(() -> new EntityNotFoundException("Пожертвование", id)); // Выбрасываем исключение, если записи нет.
    } // Завершаем блок.

    public void update(int id, String purpose, DonationCategory category, BigDecimal amount) { // Изменяет пожертвование, если оно не завершено.
        Donation donation = findById(id); // Получаем данные из репозитория.

        if (donation.getStatus() == DonationStatus.COMPLETED) { // Проверяем условие.
            throw new BusinessException("Завершённое пожертвование нельзя изменить"); // Выбрасываем исключение.
        } // Завершаем блок.

        validate(purpose, amount); // Проверяем бизнес-правила.
        donation.setPurpose(purpose.trim()); // Сохраняем значение в объекте.
        donation.setCategory(category); // Сохраняем значение в объекте.
        donation.setAmount(amount); // Сохраняем значение в объекте.
        repository.update(donation); // Сохраняем данные.
    } // Завершаем блок.

    /** Смена статуса с проверкой допустимого перехода. */
    public void changeStatus(int id, DonationStatus newStatus) { // Меняет статус, если переход разрешён enum'ом.
        Donation donation = findById(id); // Получаем данные из репозитория.

        if (!donation.getStatus().canChangeTo(newStatus)) { // Проверяем условие.
            throw new BusinessException("Недопустимый переход статуса: " // Выбрасываем исключение.
                    + donation.getStatus().getTitle() + " -> " + newStatus.getTitle()); // Формируем текст ошибки.
        } // Завершаем блок.

        donation.setStatus(newStatus); // Сохраняем значение в объекте.
        repository.update(donation); // Сохраняем данные.
    } // Завершаем блок.

    public void delete(int id) { // Удаляет пожертвование, кроме завершённого.
        Donation donation = findById(id); // Получаем данные из репозитория.

        if (donation.getStatus() == DonationStatus.COMPLETED) { // Проверяем условие.
            throw new BusinessException("Завершённое пожертвование нельзя удалить — оно входит в отчётность фонда"); // Выбрасываем исключение.
        } // Завершаем блок.

        repository.delete(id); // Удаляем элемент или запись.
    } // Завершаем блок.

    /** Поиск по назначению пожертвования. */
    public List<Donation> searchByPurpose(String text) { // Ищет пожертвования по назначению.
        requireText(text); // Проверяем строку поиска.
        return repository.searchByPurpose(text.trim()); // Возвращаем результат.
    } // Завершаем блок.

    /** Поиск по имени донора. */
    public List<Donation> searchByDonorName(String text) { // Ищет пожертвования по имени донора.
        requireText(text); // Проверяем строку поиска.
        return repository.searchByDonorName(text.trim()); // Возвращаем результат.
    } // Завершаем блок.

    /** Поиск за период. */
    public List<Donation> searchByDateRange(LocalDate from, LocalDate to) { // Ищет пожертвования за указанный период.
        if (from.isAfter(to)) { // Проверяем условие.
            throw new BusinessException("Дата начала не может быть позже даты окончания"); // Выбрасываем исключение.
        } // Завершаем блок.
        return repository.searchByDateRange(from, to); // Возвращаем результат.
    } // Завершаем блок.

    /** Фильтрация по статусу. */
    public List<Donation> filterByStatus(DonationStatus status) { // Отбирает пожертвования с нужным статусом.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .filter(donation -> donation.getStatus() == status) // Фильтруем элементы.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Фильтрация по направлению помощи. */
    public List<Donation> filterByCategory(DonationCategory category) { // Отбирает пожертвования нужного направления.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .filter(donation -> donation.getCategory() == category) // Фильтруем элементы.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Фильтрация по диапазону сумм. */
    public List<Donation> filterByAmountRange(BigDecimal from, BigDecimal to) { // Отбирает пожертвования в диапазоне сумм.
        if (from.compareTo(to) > 0) { // Проверяем условие.
            throw new BusinessException("Минимальная сумма не может быть больше максимальной"); // Выбрасываем исключение.
        } // Завершаем блок.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .filter(donation -> donation.getAmount().compareTo(from) >= 0 // Фильтруем элементы.
                        && donation.getAmount().compareTo(to) <= 0) // Проверяем верхнюю границу.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Фильтрация по донору. */
    public List<Donation> filterByDonor(int donorId) { // Отбирает пожертвования конкретного донора.
        donorService.findById(donorId); // Проверяем существование донора.
        return repository.findByDonorId(donorId); // Возвращаем результат.
    } // Завершаем блок.

    /** Сортировка по сумме. */
    public List<Donation> sortedByAmount(boolean descending) { // Сортирует по сумме: параметр задаёт направление.
        Comparator<Donation> byAmount = Comparator.comparing(Donation::getAmount); // Создаем переменную или объект.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .sorted(descending ? byAmount.reversed() : byAmount) // Сортируем элементы.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Сортировка по дате. */
    public List<Donation> sortedByDate() { // Сортирует пожертвования от старых к новым.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .sorted(Comparator.comparing(Donation::getCreatedAt)) // Сортируем элементы.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Сортировка по имени донора и сумме. */
    public List<Donation> sortedByDonorName() { // Сортирует по имени донора, затем по сумме.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .sorted(Comparator.comparing(Donation::getDonorName).thenComparing(Donation::getAmount)) // Сортируем элементы.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Проверка назначения и суммы пожертвования. */
    private void validate(String purpose, BigDecimal amount) { // Проверяет назначение и допустимую сумму.
        if (purpose == null || purpose.trim().length() < 3) { // Проверяем условие.
            throw new BusinessException("Назначение пожертвования обязательно (минимум 3 символа)"); // Выбрасываем исключение.
        } // Завершаем блок.
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) { // Проверяем условие.
            throw new BusinessException("Сумма пожертвования должна быть больше нуля"); // Выбрасываем исключение.
        } // Завершаем блок.
        if (amount.compareTo(MAX_AMOUNT) > 0) { // Проверяем условие.
            throw new BusinessException("Сумма одного пожертвования не может превышать " + MAX_AMOUNT + " руб."); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Проверка непустой строки поиска. */
    private void requireText(String text) { // Не даёт искать по пустой строке.
        if (text == null || text.trim().isEmpty()) { // Проверяем условие.
            throw new BusinessException("Строка поиска не может быть пустой"); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.
} // Завершаем блок.
