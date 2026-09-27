package ru.mirea.fund.exception;

/** Нарушение бизнес-правила фонда (некорректная сумма, запрещённый переход статуса и т.д.). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
