package ru.mirea.fund.model; // Пакет доменной модели: классы-сущности фонда.

import ru.mirea.fund.exception.BusinessException; // Своё исключение: им отвечаем на недопустимый ввод направления.

// Направление благотворительной помощи — второй enum проекта.
// Устроен так же, как DonationStatus, только без правил переходов:
// направление у пожертвования просто есть и по стадиям не меняется.
public enum DonationCategory {

    MEDICINE("Лечение"), // Сборы на лечение, операции и реабилитацию.
    EDUCATION("Образование"), // Учебники, оборудование школ, стипендии.
    CHILDREN("Помощь детям"), // Детские дома, лагеря, подарки детям.
    ANIMALS("Помощь животным"), // Приюты, корм, лечение животных.
    ECOLOGY("Экология"); // Посадка деревьев, очистка территорий.

    private final String title; // Понятное название для вывода пользователю вместо кода константы.

    // Конструктор enum всегда приватный: создать новую константу снаружи нельзя.
    DonationCategory(String title) {
        this.title = title;
    }

    public String getTitle() { // Название читают меню, отчёт статистики и файлы экспорта.
        return title;
    }

    // Разбор направления из того, что ввёл пользователь.
    // Как и у статусов, свой метод даёт понятную ошибку вместо IllegalArgumentException.
    public static DonationCategory parse(String text) {
        for (DonationCategory category : values()) { // values() возвращает массив всех констант перечисления.
            if (category.name().equalsIgnoreCase(text.trim())) { // Сравниваем без учёта регистра: "medicine" и "MEDICINE" одинаковы.
                return category;
            }
        }
        throw new BusinessException("Недопустимая категория: " + text); // Ввод не совпал ни с одной константой.
    }

    // Показываем код и название сразу: MEDICINE (Лечение) — так пользователю понятнее, что вводить.
    @Override
    public String toString() {
        return name() + " (" + title + ")";
    }
}
