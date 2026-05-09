package com.example.restaurantreservation.aggregator.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String resourceName, Object id) {
        super(resourceName + " not found with id: " + id);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
