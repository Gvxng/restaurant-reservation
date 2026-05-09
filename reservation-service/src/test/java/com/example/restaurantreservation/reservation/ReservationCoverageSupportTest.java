package com.example.restaurantreservation.reservation;

import com.example.restaurantreservation.RestaurantReservationApplication;
import com.example.restaurantreservation.exception.BusinessRuleViolationException;
import com.example.restaurantreservation.exception.DuplicateTableNumberException;
import com.example.restaurantreservation.exception.ErrorResponse;
import com.example.restaurantreservation.exception.GlobalExceptionHandler;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.exception.TableAlreadyBookedException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ReservationCoverageSupportTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void globalExceptionHandlerMapsDomainAndValidationFailures() {
        ResponseEntity<ErrorResponse> notFound = handler.handleNotFound(new ResourceNotFoundException("TableBooking", 99L));
        ResponseEntity<ErrorResponse> conflict = handler.handleConflict(new BusinessRuleViolationException("rule failed"));
        ResponseEntity<ErrorResponse> duplicate = handler.handleConflict(new DuplicateTableNumberException("duplicate"));
        ResponseEntity<ErrorResponse> booked = handler.handleConflict(new TableAlreadyBookedException("booked"));
        ResponseEntity<ErrorResponse> illegalArgument = handler.handleIllegalArgument(new IllegalArgumentException("bad id"));
        ResponseEntity<ErrorResponse> general = handler.handleGeneral(new RuntimeException("boom"));

        MethodArgumentNotValidException validationException = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "customerId", "Customer ID is required"),
                new FieldError("request", "partySize", "Party size must be at least 1")
        ));
        ResponseEntity<ErrorResponse> validation = handler.handleValidation(validationException);

        assertError(notFound, HttpStatus.NOT_FOUND, "Not Found", "TableBooking not found");
        assertError(conflict, HttpStatus.CONFLICT, "Business Rule Violation", "rule failed");
        assertError(duplicate, HttpStatus.CONFLICT, "Business Rule Violation", "duplicate");
        assertError(booked, HttpStatus.CONFLICT, "Business Rule Violation", "booked");
        assertError(illegalArgument, HttpStatus.BAD_REQUEST, "Bad Request", "bad id");
        assertError(general, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "boom");
        assertError(validation, HttpStatus.BAD_REQUEST, "Validation Error", "Customer ID is required");
        assertThat(validation.getBody().getMessage()).contains("Party size must be at least 1");
    }

    @Test
    void resourceNotFoundMessageConstructorIsCovered() {
        ResourceNotFoundException exception = new ResourceNotFoundException("custom missing");

        assertThat(exception.getMessage()).isEqualTo("custom missing");
    }

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--server.port=0"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            RestaurantReservationApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(RestaurantReservationApplication.class, args));
        }
    }

    private void assertError(
            ResponseEntity<ErrorResponse> response,
            HttpStatus status,
            String error,
            String messageSnippet) {

        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(status.value());
        assertThat(response.getBody().getError()).isEqualTo(error);
        assertThat(response.getBody().getMessage()).contains(messageSnippet);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
