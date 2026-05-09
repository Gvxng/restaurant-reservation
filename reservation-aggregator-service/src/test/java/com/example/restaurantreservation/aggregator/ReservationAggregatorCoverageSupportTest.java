package com.example.restaurantreservation.aggregator;

import com.example.restaurantreservation.RestaurantReservationApplication;
import com.example.restaurantreservation.WebClientConfig;
import com.example.restaurantreservation.aggregator.exception.DownstreamServiceException;
import com.example.restaurantreservation.aggregator.exception.ErrorResponse;
import com.example.restaurantreservation.aggregator.exception.GlobalExceptionHandler;
import com.example.restaurantreservation.aggregator.exception.InvalidInputException;
import com.example.restaurantreservation.aggregator.exception.MenuItemUnavailableException;
import com.example.restaurantreservation.aggregator.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ReservationAggregatorCoverageSupportTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void globalExceptionHandlerMapsAggregatorFailures() {
        ResponseEntity<ErrorResponse> notFound = handler.handleNotFound(new NotFoundException("ReservationAggregate", "missing"));
        ResponseEntity<ErrorResponse> invalid = handler.handleInvalidInput(new InvalidInputException("invalid request"));
        ResponseEntity<ErrorResponse> unavailable = handler.handleMenuUnavailable(new MenuItemUnavailableException(10L));
        ResponseEntity<ErrorResponse> downstream = handler.handleDownstream(new DownstreamServiceException("service unavailable"));
        ResponseEntity<ErrorResponse> general = handler.handleGeneral(new RuntimeException("boom"));

        MethodArgumentNotValidException validationException = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "customerId", "Customer ID is required"),
                new FieldError("request", "preOrderItems", "At least one pre-order item is required")
        ));
        ResponseEntity<ErrorResponse> validation = handler.handleValidation(validationException);

        assertError(notFound, HttpStatus.NOT_FOUND, "Not Found", "ReservationAggregate not found");
        assertError(invalid, HttpStatus.BAD_REQUEST, "Invalid Input", "invalid request");
        assertError(unavailable, HttpStatus.CONFLICT, "Menu Item Unavailable", "Menu item 10");
        assertError(downstream, HttpStatus.SERVICE_UNAVAILABLE, "Downstream Service Error", "service unavailable");
        assertError(general, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "boom");
        assertError(validation, HttpStatus.BAD_REQUEST, "Validation Error", "Customer ID is required");
        assertThat(validation.getBody().getMessage()).contains("At least one pre-order item is required");
    }

    @Test
    void overloadedExceptionConstructorsAreCovered() {
        assertThat(new NotFoundException("custom missing").getMessage()).isEqualTo("custom missing");
        assertThat(new DownstreamServiceException("bad gateway", HttpStatus.BAD_GATEWAY).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void webClientConfigCreatesBuilder() {
        WebClient.Builder builder = new WebClientConfig().webClientBuilder();

        assertThat(builder).isNotNull();
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
