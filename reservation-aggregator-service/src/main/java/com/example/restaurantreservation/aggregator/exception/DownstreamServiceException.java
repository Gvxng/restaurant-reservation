package com.example.restaurantreservation.aggregator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class DownstreamServiceException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public DownstreamServiceException(String message) {
        this(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public DownstreamServiceException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
