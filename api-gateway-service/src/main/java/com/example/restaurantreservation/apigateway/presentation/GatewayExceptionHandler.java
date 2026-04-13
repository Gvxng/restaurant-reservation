package com.example.restaurantreservation.apigateway.presentation;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleDownstreamHttpError(HttpStatusCodeException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> handleUnavailableService(ResourceAccessException ex) {
        return ResponseEntity.status(503)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"" + ex.getMessage() + "\"}");
    }
}
