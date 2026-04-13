package com.example.restaurantreservation.exception;

public class NegativePointsBalanceException extends BusinessRuleViolationException {

    public NegativePointsBalanceException(String message) {
        super(message);
    }
}
