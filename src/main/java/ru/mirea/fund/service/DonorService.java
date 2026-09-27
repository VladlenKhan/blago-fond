package ru.mirea.fund.service; // Объявляем пакет класса.

import ru.mirea.fund.exception.BusinessException; // Подключаем необходимый тип.
import ru.mirea.fund.exception.EntityNotFoundException; // Подключаем необходимый тип.
import ru.mirea.fund.model.Donor; // Подключаем необходимый тип.
import ru.mirea.fund.repository.DonorRepository; // Подключаем необходимый тип.

import java.util.Comparator; // Подключаем необходимый тип.
import java.util.List; // Подключаем необходимый тип.
import java.util.stream.Collectors; // Подключаем необходимый тип.

/** Бизнес-логика работы с донорами: проверки правил и вызов репозитория. */
public class DonorService { // Сервис доноров: проверяет правила и обращается к репозиторию.

    private final DonorRepository repository = new DonorRepository(); // Репозиторий доноров, через него идёт работа с БД.

    /** Создание донора: ФИО, email и город обязательны, email уникален. */
    public Donor create(String fullName, String email, String phone, String city) { // Создаёт донора после проверки правил.
        validate(fullName, email, city); // Проверяем бизнес-правила.

        if (repository.existsByEmail(email, 0)) { // Проверяем условие.
            throw new BusinessException("Донор с email " + email + " уже зарегистрирован"); // Выбрасываем исключение.
        } // Завершаем блок.

        Donor donor = new Donor(fullName.trim(), email.trim(), phone, city.trim()); // Создаем переменную или объект.
        repository.save(donor); // Сохраняем данные.
        return donor; // Возвращаем результат.
    } // Завершаем блок.

    public List<Donor> findAll() { // Возвращает список всех доноров.
        return repository.findAll(); // Возвращаем результат.
    } // Завершаем блок.

    public Donor findById(int id) { // Находит донора или сообщает, что его нет.
        return repository.findById(id) // Получаем данные из репозитория.
                .orElseThrow(() -> new EntityNotFoundException("Донор", id)); // Выбрасываем исключение, если записи нет.
    } // Завершаем блок.

    public void update(int id, String fullName, String email, String phone, String city) { // Изменяет данные донора после проверок.
        Donor donor = findById(id); // Получаем данные из репозитория.
        validate(fullName, email, city); // Проверяем бизнес-правила.

        if (repository.existsByEmail(email, id)) { // Проверяем условие.
            throw new BusinessException("Email " + email + " уже занят другим донором"); // Выбрасываем исключение.
        } // Завершаем блок.

        donor.setFullName(fullName.trim()); // Сохраняем значение в объекте.
        donor.setEmail(email.trim()); // Сохраняем значение в объекте.
        donor.setPhone(phone); // Сохраняем значение в объекте.
        donor.setCity(city.trim()); // Сохраняем значение в объекте.
        repository.update(donor); // Сохраняем данные.
    } // Завершаем блок.

    public void delete(int id) { // Удаляет донора, если он существует.
        findById(id); // Проверяем существование записи.
        repository.delete(id); // Удаляем элемент или запись.
    } // Завершаем блок.

    /** Поиск донора по имени или email. */
    public List<Donor> search(String text) { // Ищет доноров по имени или email.
        if (text == null || text.trim().isEmpty()) { // Проверяем условие.
            throw new BusinessException("Строка поиска не может быть пустой"); // Выбрасываем исключение.
        } // Завершаем блок.
        return repository.searchByText(text.trim()); // Возвращаем результат.
    } // Завершаем блок.

    /** Фильтрация доноров по городу. */
    public List<Donor> filterByCity(String city) { // Отбирает доноров нужного города.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .filter(donor -> donor.getCity().equalsIgnoreCase(city.trim())) // Фильтруем элементы.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Сортировка доноров по алфавиту. */
    public List<Donor> sortedByName() { // Возвращает доноров по алфавиту.
        return repository.findAll().stream() // Создаем поток обработки данных.
                .sorted(Comparator.comparing(Donor::getFullName)) // Сортируем элементы.
                .collect(Collectors.toList()); // Собираем результат в коллекцию.
    } // Завершаем блок.

    /** Проверка обязательных полей донора. */
    private void validate(String fullName, String email, String city) { // Проверяет обязательные поля и формат email.
        if (fullName == null || fullName.trim().length() < 3) { // Проверяем условие.
            throw new BusinessException("ФИО донора обязательно и должно содержать минимум 3 символа"); // Выбрасываем исключение.
        } // Завершаем блок.
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[a-zA-Z]{2,}")) { // Проверяем условие.
            throw new BusinessException("Некорректный email: " + email); // Выбрасываем исключение.
        } // Завершаем блок.
        if (city == null || city.trim().isEmpty()) { // Проверяем условие.
            throw new BusinessException("Город обязателен для заполнения"); // Выбрасываем исключение.
        } // Завершаем блок.
    } // Завершаем блок.
} // Завершаем блок.
