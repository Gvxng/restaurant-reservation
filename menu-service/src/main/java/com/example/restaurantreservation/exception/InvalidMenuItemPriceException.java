package com.example.restaurantreservation.exception;

public class InvalidMenuItemPriceException extends BusinessRuleViolationException {

    public InvalidMenuItemPriceException(String message) {
        super(message);
    }
}
