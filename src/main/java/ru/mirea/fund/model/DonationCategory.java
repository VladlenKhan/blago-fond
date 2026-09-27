package ru.mirea.fund.model;

import ru.mirea.fund.exception.BusinessException;

/** Направление благотворительной помощи. */
public enum DonationCategory {

    MEDICINE("Лечение"),
    EDUCATION("Образование"),
    CHILDREN("Помощь детям"),
    ANIMALS("Помощь животным"),
    ECOLOGY("Экология");

    private final String title;

    DonationCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static DonationCategory parse(String text) {
        for (DonationCategory category : values()) {
            if (category.name().equalsIgnoreCase(text.trim())) {
                return category;
            }
        }
        throw new BusinessException("Недопустимая категория: " + text);
    }

    @Override
    public String toString() {
        return name() + " (" + title + ")";
    }
}
