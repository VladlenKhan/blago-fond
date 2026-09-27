package ru.mirea.fund.service; // Пакет бизнес-логики: здесь проверяются все правила фонда.

import ru.mirea.fund.exception.BusinessException; // Бросаем, когда пользователь нарушил правило фонда.
import ru.mirea.fund.exception.EntityNotFoundException; // Бросаем, когда пожертвования с таким id в базе нет.
import ru.mirea.fund.model.Donation; // Класс-сущность, с объектами которого работает сервис.
import ru.mirea.fund.model.DonationCategory; // Enum направления помощи: ограничивает допустимые значения.
import ru.mirea.fund.model.DonationStatus; // Enum статуса: он же хранит правила переходов между стадиями.
import ru.mirea.fund.repository.DonationRepository; // Слой доступа к данным: только через него идём в базу.

import java.math.BigDecimal; // Точный тип для денег: сравнивается через compareTo, а не через > и <.
import java.time.LocalDate; // Дата без времени: её вводит пользователь при поиске за период.
import java.util.Comparator; // Задаёт правило сравнения объектов для сортировки списка.
import java.util.List; // Тип возвращаемого набора пожертвований.
import java.util.stream.Collectors; // Собирает поток обратно в List после фильтрации или сортировки.

// Главный сервис системы — вся бизнес-логика пожертвований.
//
// БИЗНЕС-ПРАВИЛА, реализованные в этом классе:
// 1. Назначение обязательно, минимум 3 символа.
// 2. Сумма строго больше нуля и не превышает 1 000 000 руб.
// 3. Донор должен существовать в базе.
// 4. Разрешены только переходы NEW -> CONFIRMED -> COMPLETED (отмена — до завершения).
// 5. Завершённое пожертвование нельзя изменить или удалить.
//
// Важно: правила проверяются здесь, в коде, а не в меню. Если завтра появится
// веб-интерфейс вместо консоли — правила продолжат работать без изменений.
public class DonationService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000"); // Потолок одного пожертвования вынесен в константу, чтобы число не терялось в середине кода.

    private final DonationRepository repository = new DonationRepository(); // Репозиторий пожертвований: через него идут все обращения к базе.
    private final DonorService donorService = new DonorService(); // Сервис доноров: переиспользуем его проверку существования донора, а не пишем её заново.

    // Создание пожертвования: проверяем донора, назначение и сумму.
    public Donation create(int donorId, String purpose, DonationCategory category, BigDecimal amount) {
        donorService.findById(donorId); // ПРАВИЛО 3: если донора нет, findById сам кинет EntityNotFoundException.
        validate(purpose, amount); // ПРАВИЛА 1 и 2: назначение и сумма.

        Donation donation = new Donation(donorId, purpose.trim(), category, amount);
        repository.save(donation);

        // Перечитываем запись из базы: в объекте после save нет имени донора (donorName),
        // а оно нужно, чтобы сразу показать пользователю созданное пожертвование.
        return findById(donation.getId());
    }

    // Отдаёт все пожертвования: используется списком, фильтрами и статистикой.
    public List<Donation> findAll() {
        return repository.findAll();
    }

    // Поиск по id: пустой Optional из репозитория превращаем в понятную ошибку.
    public Donation findById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пожертвование", id));
    }

    // Изменение пожертвования: назначение, направление и сумма.
    public void update(int id, String purpose, DonationCategory category, BigDecimal amount) {
        Donation donation = findById(id);

        // ПРАВИЛО 5: завершённые записи входят в отчётность фонда, их трогать нельзя.
        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new BusinessException("Завершённое пожертвование нельзя изменить");
        }

        validate(purpose, amount);
        donation.setPurpose(purpose.trim());
        donation.setCategory(category);
        donation.setAmount(amount);
        repository.update(donation);
    }

    // ПРАВИЛО 4: смена статуса.
    // Сам список разрешённых переходов лежит в enum DonationStatus — сервис только
    // спрашивает «можно ли?» и формирует понятное сообщение об ошибке.
    public void changeStatus(int id, DonationStatus newStatus) {
        Donation donation = findById(id);

        if (!donation.getStatus().canChangeTo(newStatus)) {
            throw new BusinessException("Недопустимый переход статуса: "
                    + donation.getStatus().getTitle() + " -> " + newStatus.getTitle());
        }

        donation.setStatus(newStatus);
        repository.update(donation);
    }

    // Удаление пожертвования с той же защитой завершённых записей (ПРАВИЛО 5).
    public void delete(int id) {
        Donation donation = findById(id);

        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new BusinessException("Завершённое пожертвование нельзя удалить — оно входит в отчётность фонда");
        }

        repository.delete(id);
    }

    // ---------- ПОИСК: выполняется базой данных через SQL и PreparedStatement ----------
    // Поиск отдан базе, потому что она ищет по тексту быстрее и не тянет все строки в память программы.

    // Поиск по части назначения пожертвования.
    public List<Donation> searchByPurpose(String text) {
        requireText(text);
        return repository.searchByPurpose(text.trim());
    }

    // Поиск по имени донора — работает за счёт JOIN в репозитории.
    public List<Donation> searchByDonorName(String text) {
        requireText(text);
        return repository.searchByDonorName(text.trim());
    }

    // Поиск пожертвований за период между двумя датами.
    public List<Donation> searchByDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("Дата начала не может быть позже даты окончания"); // Проверка здравого смысла: «с 2025 по 2024» — ошибка пользователя.
        }
        return repository.searchByDateRange(from, to);
    }

    // ---------- ФИЛЬТРАЦИЯ: выполняется в Java через Stream API ----------
    // Здесь наоборот: берём список из базы и отбираем нужное в памяти,
    // чтобы показать работу с коллекциями и Stream API, как требует задание.

    // Фильтр 1: по статусу. filter оставляет только подходящие элементы потока.
    public List<Donation> filterByStatus(DonationStatus status) {
        return repository.findAll().stream()
                .filter(donation -> donation.getStatus() == status) // Константы enum сравниваем через ==, это один и тот же объект.
                .collect(Collectors.toList());
    }

    // Фильтр 2: по направлению помощи.
    public List<Donation> filterByCategory(DonationCategory category) {
        return repository.findAll().stream()
                .filter(donation -> donation.getCategory() == category)
                .collect(Collectors.toList());
    }

    // Фильтр 3: по диапазону сумм.
    public List<Donation> filterByAmountRange(BigDecimal from, BigDecimal to) {
        if (from.compareTo(to) > 0) {
            throw new BusinessException("Минимальная сумма не может быть больше максимальной");
        }
        return repository.findAll().stream()
                // BigDecimal — объект, а не примитив, поэтому сравниваем через compareTo:
                // результат >= 0 значит «больше либо равно», <= 0 — «меньше либо равно».
                .filter(donation -> donation.getAmount().compareTo(from) >= 0
                        && donation.getAmount().compareTo(to) <= 0)
                .collect(Collectors.toList());
    }

    // Фильтр 4: по донору — тут выгоднее сразу спросить базу с условием WHERE, чем тянуть всё в память.
    public List<Donation> filterByDonor(int donorId) {
        donorService.findById(donorId); // Сначала убеждаемся, что такой донор существует.
        return repository.findByDonorId(donorId);
    }

    // ---------- СОРТИРОВКА через Comparator ----------

    // Сортировка 1: по сумме. Параметр descending = true ставит крупные пожертвования первыми.
    public List<Donation> sortedByAmount(boolean descending) {
        Comparator<Donation> byAmount = Comparator.comparing(Donation::getAmount);
        return repository.findAll().stream()
                .sorted(descending ? byAmount.reversed() : byAmount) // reversed() переворачивает порядок сравнения на обратный.
                .collect(Collectors.toList());
    }

    // Сортировка 2: по дате, от старых записей к новым.
    public List<Donation> sortedByDate() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Donation::getCreatedAt))
                .collect(Collectors.toList());
    }

    // Сортировка 3: по имени донора, а внутри одного донора — по сумме (за это отвечает thenComparing).
    public List<Donation> sortedByDonorName() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Donation::getDonorName).thenComparing(Donation::getAmount))
                .collect(Collectors.toList());
    }

    // Проверка полей пожертвования: используется и при создании, и при изменении.
    private void validate(String purpose, BigDecimal amount) {
        if (purpose == null || purpose.trim().length() < 3) {
            throw new BusinessException("Назначение пожертвования обязательно (минимум 3 символа)");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) { // compareTo(ZERO) <= 0 означает «меньше либо равно нулю».
            throw new BusinessException("Сумма пожертвования должна быть больше нуля");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessException("Сумма одного пожертвования не может превышать " + MAX_AMOUNT + " руб.");
        }
    }

    // Не даёт искать по пустой строке: иначе LIKE '%%' вернул бы все записи подряд.
    private void requireText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Строка поиска не может быть пустой");
        }
    }
}
