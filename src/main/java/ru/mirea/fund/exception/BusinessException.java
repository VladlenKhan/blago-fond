package ru.mirea.fund.exception; // Пакет собственных исключений системы.

// Нарушено бизнес-правило фонда: отрицательная сумма, пустое назначение,
// занятый email, запрещённый переход статуса и тому подобное.
// Наследуемся от RuntimeException (непроверяемое исключение), чтобы не писать throws
// в каждом методе сервиса: все такие ошибки ловятся в одном месте — в меню ConsoleApp,
// где пользователь видит понятный текст, а программа продолжает работу.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) { // Принимает готовый текст нарушенного правила.
        super(message); // Передаём сообщение в RuntimeException, оттуда его достанет getMessage().
    }
}
