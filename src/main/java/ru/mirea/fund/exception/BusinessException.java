package ru.mirea.fund.exception; // Объявляем пакет класса.

/** Собственное исключение: нарушено бизнес-правило фонда. */
public class BusinessException extends RuntimeException { // Ошибка бизнес-правила: непроверяемое исключение.

    public BusinessException(String message) { // Конструктор: принимает текст нарушенного правила.
        super(message); // Вызываем конструктор суперкласса.
    } // Завершаем блок.
} // Завершаем блок.
