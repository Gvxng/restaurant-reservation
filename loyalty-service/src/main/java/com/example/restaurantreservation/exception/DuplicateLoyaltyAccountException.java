package com.example.restaurantreservation.exception;

public class DuplicateLoyaltyAccountException extends BusinessRuleViolationException {

    public DuplicateLoyaltyAccountException(String message) {
        super(message);
    }
}
