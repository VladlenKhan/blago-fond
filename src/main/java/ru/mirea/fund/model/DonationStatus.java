package ru.mirea.fund.model;

import ru.mirea.fund.exception.BusinessException;

/** Статус пожертвования и разрешённые переходы между статусами. */
public enum DonationStatus {

    NEW("Новое"),
    CONFIRMED("Подтверждено"),
    COMPLETED("Завершено"),
    CANCELLED("Отменено");

    private final String title;

    DonationStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /** Бизнес-правило: разрешён только переход NEW -> CONFIRMED -> COMPLETED, отмена возможна до завершения. */
    public boolean canChangeTo(DonationStatus next) {
        switch (this) {
            case NEW:
                return next == CONFIRMED || next == CANCELLED;
            case CONFIRMED:
                return next == COMPLETED || next == CANCELLED;
            default:
                return false;
        }
    }

    /** Разбор статуса из строки с понятной ошибкой вместо IllegalArgumentException. */
    public static DonationStatus parse(String text) {
        for (DonationStatus status : values()) {
            if (status.name().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        throw new BusinessException("Недопустимый статус: " + text);
    }

    @Override
    public String toString() {
        return name() + " (" + title + ")";
    }
}
