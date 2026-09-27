package ru.mirea.fund.model; // Пакет доменной модели: классы-сущности фонда.

import ru.mirea.fund.exception.BusinessException; // Своё исключение: им отвечаем на недопустимый ввод статуса.

// Статус пожертвования.
// Список статусов закрытый и известен заранее, поэтому это enum, а не String:
// опечатка вроде "CONFIRMD" не скомпилируется, а строка спокойно попала бы в базу.
// В таблице donations на столбец status дополнительно стоит CHECK с теми же значениями.
public enum DonationStatus {

    NEW("Новое"), // Пожертвование создано, деньги ещё не подтверждены.
    CONFIRMED("Подтверждено"), // Средства поступили, фонд принял сбор в работу.
    COMPLETED("Завершено"), // Помощь оказана; такие записи входят в отчётность и защищены от правок.
    CANCELLED("Отменено"); // Сбор отменён и дальше не обрабатывается.

    private final String title; // Понятное название для вывода пользователю вместо кода константы.

    // Конструктор enum всегда приватный: создать новую константу снаружи нельзя.
    DonationStatus(String title) {
        this.title = title;
    }

    public String getTitle() { // Название читают меню, отчёт статистики и файлы экспорта.
        return title;
    }

    // БИЗНЕС-ПРАВИЛО: разрешены только переходы NEW -> CONFIRMED -> COMPLETED,
    // отменить сбор можно на любой стадии до завершения.
    // Правило живёт прямо здесь, потому что это знание о самих статусах:
    // сервис только спрашивает «можно ли?», а список переходов не хранит.
    public boolean canChangeTo(DonationStatus next) {
        switch (this) {
            case NEW: // Новое можно подтвердить или отменить.
                return next == CONFIRMED || next == CANCELLED;
            case CONFIRMED: // Подтверждённое можно завершить или отменить.
                return next == COMPLETED || next == CANCELLED;
            default: // COMPLETED и CANCELLED — конечные состояния, из них выхода нет.
                return false;
        }
    }

    // Разбор статуса из того, что ввёл пользователь.
    // Свой метод нужен вместо стандартного valueOf(): тот кидает IllegalArgumentException
    // с непонятным текстом и не прощает регистр, а здесь мы сами даём внятное сообщение.
    public static DonationStatus parse(String text) {
        for (DonationStatus status : values()) { // values() возвращает массив всех констант перечисления.
            if (status.name().equalsIgnoreCase(text.trim())) { // Сравниваем без учёта регистра: "new" и "NEW" одинаковы.
                return status;
            }
        }
        throw new BusinessException("Недопустимый статус: " + text); // Ввод не совпал ни с одной константой.
    }

    // Показываем код и название сразу: NEW (Новое) — так пользователю понятнее, что вводить.
    @Override
    public String toString() {
        return name() + " (" + title + ")";
    }
}
