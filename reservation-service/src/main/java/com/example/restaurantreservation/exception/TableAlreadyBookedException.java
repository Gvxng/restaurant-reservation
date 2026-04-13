package com.example.restaurantreservation.exception;

public class TableAlreadyBookedException extends BusinessRuleViolationException {

    public TableAlreadyBookedException(String message) {
        super(message);
    }
}
