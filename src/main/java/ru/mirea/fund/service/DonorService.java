package ru.mirea.fund.service; // Пакет бизнес-логики: здесь проверяются все правила фонда.

import ru.mirea.fund.exception.BusinessException; // Бросаем, когда пользователь нарушил правило фонда.
import ru.mirea.fund.exception.EntityNotFoundException; // Бросаем, когда донора с таким id в базе нет.
import ru.mirea.fund.model.Donor; // Класс-сущность, с объектами которого работает сервис.
import ru.mirea.fund.repository.DonorRepository; // Слой доступа к данным: только через него идём в базу.

import java.util.Comparator; // Задаёт правило сравнения объектов для сортировки списка.
import java.util.List; // Тип возвращаемого набора доноров.
import java.util.stream.Collectors; // Собирает поток обратно в List после фильтрации или сортировки.

// Сервисный слой для доноров: проверки бизнес-правил плюс вызов репозитория.
// Меню (ConsoleApp) обращается только сюда и не знает ни про SQL, ни про JDBC.
// Сервис, наоборот, не знает, как выводить данные на экран.
public class DonorService {

    private final DonorRepository repository = new DonorRepository(); // Репозиторий доноров: сервис пользуется им для всех обращений к базе.

    // Создание донора.
    // ПРАВИЛА: ФИО, email и город обязательны и корректны (проверяет validate),
    // плюс email не должен повторяться.
    public Donor create(String fullName, String email, String phone, String city) {
        validate(fullName, email, city);

        // Проверяем уникальность до вставки, чтобы показать понятное сообщение.
        // В самой базе на email стоит UNIQUE — это вторая линия защиты.
        if (repository.existsByEmail(email, 0)) {
            throw new BusinessException("Донор с email " + email + " уже зарегистрирован");
        }

        Donor donor = new Donor(fullName.trim(), email.trim(), phone, city.trim()); // trim() убирает случайные пробелы по краям ввода.
        repository.save(donor); // После сохранения у объекта появится id, выданный базой.
        return donor;
    }

    // Отдаёт всех доноров: используется в списках меню и при создании пожертвования.
    public List<Donor> findAll() {
        return repository.findAll();
    }

    // Поиск по id. Репозиторий возвращает Optional, а сервис превращает пустой Optional
    // в исключение: наверху удобнее один раз поймать ошибку, чем каждый раз проверять «а есть ли значение».
    public Donor findById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Донор", id));
    }

    // Изменение данных донора с теми же проверками, что и при создании.
    public void update(int id, String fullName, String email, String phone, String city) {
        Donor donor = findById(id); // Заодно убеждаемся, что такой донор существует.
        validate(fullName, email, city);

        // Передаём id, чтобы собственный email донора не считался занятым.
        if (repository.existsByEmail(email, id)) {
            throw new BusinessException("Email " + email + " уже занят другим донором");
        }

        // Меняем поля у объекта, а потом одним запросом сохраняем всё сразу.
        donor.setFullName(fullName.trim());
        donor.setEmail(email.trim());
        donor.setPhone(phone);
        donor.setCity(city.trim());
        repository.update(donor);
    }

    // Удаление донора вместе с его пожертвованиями (за каскад отвечает внешний ключ в базе).
    public void delete(int id) {
        findById(id); // Если донора нет — вылетит EntityNotFoundException и до удаления дело не дойдёт.
        repository.delete(id);
    }

    // ПОИСК донора по имени или email; сам SQL-запрос лежит в репозитории.
    public List<Donor> search(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Строка поиска не может быть пустой"); // Пустая строка в LIKE '%%' вернула бы вообще всех — это не поиск.
        }
        return repository.searchByText(text.trim());
    }

    // ФИЛЬТР по городу сделан через Stream API, а не через SQL, чтобы показать работу с коллекциями:
    // stream() превращает список в поток, filter() оставляет подходящие элементы,
    // collect() собирает их обратно в список.
    public List<Donor> filterByCity(String city) {
        return repository.findAll().stream()
                .filter(donor -> donor.getCity().equalsIgnoreCase(city.trim()))
                .collect(Collectors.toList());
    }

    // СОРТИРОВКА по алфавиту: Comparator.comparing указывает, по какому полю сравнивать объекты.
    public List<Donor> sortedByName() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Donor::getFullName))
                .collect(Collectors.toList());
    }

    // Общие проверки полей донора.
    // Вынесены в отдельный приватный метод, потому что нужны и при создании, и при изменении.
    private void validate(String fullName, String email, String city) {
        if (fullName == null || fullName.trim().length() < 3) {
            throw new BusinessException("ФИО донора обязательно и должно содержать минимум 3 символа");
        }
        // Простая проверка формата: что-то, собака, что-то, точка и минимум две буквы домена.
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[a-zA-Z]{2,}")) {
            throw new BusinessException("Некорректный email: " + email);
        }
        if (city == null || city.trim().isEmpty()) {
            throw new BusinessException("Город обязателен для заполнения");
        }
    }
}
