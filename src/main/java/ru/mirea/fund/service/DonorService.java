package ru.mirea.fund.service;

import ru.mirea.fund.exception.BusinessException;
import ru.mirea.fund.exception.EntityNotFoundException;
import ru.mirea.fund.model.Donor;
import ru.mirea.fund.repository.DonorRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Бизнес-логика работы с донорами. */
public class DonorService {

    private final DonorRepository repository = new DonorRepository();

    /** Правило 1: ФИО обязательно. Правило 2: email корректный и уникальный. */
    public Donor create(String fullName, String email, String phone, String city) {
        validate(fullName, email, city);
        if (repository.existsByEmail(email, 0)) {
            throw new BusinessException("Донор с email " + email + " уже зарегистрирован");
        }
        Donor donor = new Donor(fullName.trim(), email.trim(), phone, city.trim());
        repository.save(donor);
        return donor;
    }

    public List<Donor> findAll() {
        return repository.findAll();
    }

    public Donor findById(int id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Донор", id));
    }

    public void update(int id, String fullName, String email, String phone, String city) {
        Donor donor = findById(id);
        validate(fullName, email, city);
        if (repository.existsByEmail(email, id)) {
            throw new BusinessException("Email " + email + " уже занят другим донором");
        }
        donor.setFullName(fullName.trim());
        donor.setEmail(email.trim());
        donor.setPhone(phone);
        donor.setCity(city.trim());
        repository.update(donor);
    }

    public void delete(int id) {
        findById(id);
        repository.delete(id);
    }

    /** Поиск по имени или email. */
    public List<Donor> search(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Строка поиска не может быть пустой");
        }
        return repository.searchByText(text.trim());
    }

    /** Фильтрация по городу средствами Stream API. */
    public List<Donor> filterByCity(String city) {
        return repository.findAll().stream()
                .filter(donor -> donor.getCity().equalsIgnoreCase(city.trim()))
                .collect(Collectors.toList());
    }

    /** Сортировка по имени. */
    public List<Donor> sortedByName() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Donor::getFullName))
                .collect(Collectors.toList());
    }

    private void validate(String fullName, String email, String city) {
        if (fullName == null || fullName.trim().length() < 3) {
            throw new BusinessException("ФИО донора обязательно и должно содержать минимум 3 символа");
        }
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[a-zA-Z]{2,}")) {
            throw new BusinessException("Некорректный email: " + email);
        }
        if (city == null || city.trim().isEmpty()) {
            throw new BusinessException("Город обязателен для заполнения");
        }
    }
}
