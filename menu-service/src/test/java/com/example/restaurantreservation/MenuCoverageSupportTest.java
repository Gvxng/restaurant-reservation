package com.example.restaurantreservation;

import com.example.restaurantreservation.exception.BusinessRuleViolationException;
import com.example.restaurantreservation.exception.ErrorResponse;
import com.example.restaurantreservation.exception.GlobalExceptionHandler;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("testing")
class MenuCoverageSupportTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            RestaurantReservationApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(RestaurantReservationApplication.class, args));
        }
    }

    @Test
    void handleNotFoundReturns404Response() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new ResourceNotFoundException("MenuItem", 42L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("MenuItem not found with id: 42", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleConflictReturns409Response() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(new BusinessRuleViolationException("Invalid business rule"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Business Rule Violation", response.getBody().getError());
        assertEquals("Invalid business rule", response.getBody().getMessage());
    }

    @Test
    void handleValidationReturns400Response() throws NoSuchMethodException {
        Method method = ValidationProbe.class.getDeclaredMethod("submit", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new ValidationProbe(), "validationProbe");
        bindingResult.addError(new FieldError("validationProbe", "value", "must not be blank"));
        bindingResult.addError(new FieldError("validationProbe", "value", "size must be between 2 and 20"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("must not be blank, size must be between 2 and 20", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgumentReturns400Response() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(new IllegalArgumentException("Bad input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Bad input", response.getBody().getMessage());
    }

    @Test
    void handleGeneralReturns500Response() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(new Exception("Unexpected"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Unexpected", response.getBody().getMessage());
    }

    private static class ValidationProbe {
        @SuppressWarnings("unused")
        void submit(String value) {
        }
    }
}
