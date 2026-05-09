package com.example.restaurantreservation.aggregator.exception;

public class MenuItemUnavailableException extends RuntimeException {

    public MenuItemUnavailableException(Long menuItemId) {
        super("Menu item " + menuItemId + " is unavailable and cannot be used in a reservation pre-order.");
    }
}
