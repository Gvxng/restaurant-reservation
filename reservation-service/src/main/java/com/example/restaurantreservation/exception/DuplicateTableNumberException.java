package com.example.restaurantreservation.exception;

public class DuplicateTableNumberException extends BusinessRuleViolationException {

    public DuplicateTableNumberException(String message) {
        super(message);
    }
}
