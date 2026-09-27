package ru.mirea.fund.model; // Объявляем пакет класса.

import ru.mirea.fund.exception.BusinessException; // Подключаем необходимый тип.

/** Статус пожертвования и разрешённые переходы между статусами. */
public enum DonationStatus { // Перечисление статусов: закрытый список вместо строк.

    NEW("Новое"), // Пожертвование создано, но ещё не подтверждено.
    CONFIRMED("Подтверждено"), // Деньги поступили, заявка принята в работу.
    COMPLETED("Завершено"), // Помощь оказана, запись входит в отчётность.
    CANCELLED("Отменено"); // Пожертвование отменено и дальше не обрабатывается.

    private final String title; // Понятное название статуса для вывода.

    DonationStatus(String title) { // Конструктор перечисления: задаёт название константе.
        this.title = title; // Сохраняем значение в поле.
    } // Завершаем блок.

    public String getTitle() { // Возвращает название статуса.
        return title; // Возвращаем результат.
    } // Завершаем блок.

    /** Бизнес-правило: NEW -> CONFIRMED -> COMPLETED, отмена возможна до завершения. */
    public boolean canChangeTo(DonationStatus next) { // Проверяет, разрешён ли переход в новый статус.
        switch (this) { // Выбираем сценарий выполнения.
            case NEW: // Из нового можно подтвердить или отменить.
                return next == CONFIRMED || next == CANCELLED; // Возвращаем результат.
            case CONFIRMED: // Из подтверждённого можно завершить или отменить.
                return next == COMPLETED || next == CANCELLED; // Возвращаем результат.
            default: // COMPLETED и CANCELLED — конечные состояния.
                return false; // Возвращаем результат.
        } // Завершаем блок.
    } // Завершаем блок.

    /** Разбор статуса из строки с понятной ошибкой. */
    public static DonationStatus parse(String text) { // Превращает ввод пользователя в константу статуса.
        for (DonationStatus status : values()) { // Перебираем элементы.
            if (status.name().equalsIgnoreCase(text.trim())) { // Проверяем условие.
                return status; // Возвращаем результат.
            } // Завершаем блок.
        } // Завершаем блок.
        throw new BusinessException("Недопустимый статус: " + text); // Выбрасываем исключение.
    } // Завершаем блок.

    @Override // Переопределяем метод.
    public String toString() { // Показывает код и название: NEW (Новое).
        return name() + " (" + title + ")"; // Возвращаем результат.
    } // Завершаем блок.
} // Завершаем блок.
