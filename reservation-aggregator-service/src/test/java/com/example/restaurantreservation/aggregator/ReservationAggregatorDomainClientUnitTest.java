package com.example.restaurantreservation.aggregator;

import com.example.restaurantreservation.aggregator.domainclientlayer.LoyaltyDomainClientImpl;
import com.example.restaurantreservation.aggregator.domainclientlayer.MenuDomainClientImpl;
import com.example.restaurantreservation.aggregator.domainclientlayer.ReservationDomainClientImpl;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.BookingClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreateBookingClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreatePreOrderClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.LoyaltyAccountClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.MenuItemClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.PreOrderClientResponse;
import com.example.restaurantreservation.aggregator.exception.DownstreamServiceException;
import com.example.restaurantreservation.aggregator.exception.InvalidInputException;
import com.example.restaurantreservation.aggregator.exception.NotFoundException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationAggregatorDomainClientUnitTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void reservationDomainClientForwardsBookingAndPreOrderCrudRequests() {
        ReservationDomainClientImpl client = reservationClient(baseUrl);
        CreateBookingClientRequest bookingRequest = CreateBookingClientRequest.builder()
                .customerId(101L)
                .tableId(3L)
                .reservationDate(LocalDate.now().plusDays(1))
                .timeSlotStart(LocalTime.of(18, 0))
                .timeSlotEnd(LocalTime.of(20, 0))
                .partySize(4)
                .status("CONFIRMED")
                .build();
        CreatePreOrderClientRequest preOrderRequest = CreatePreOrderClientRequest.builder()
                .bookingId(1L)
                .items(java.util.List.of(CreatePreOrderClientRequest.LineItemRequest.builder()
                        .menuItemId(10L)
                        .quantity(2)
                        .unitAmount(new BigDecimal("12.00"))
                        .currency("CAD")
                        .build()))
                .build();

        BookingClientResponse found = client.getBookingById(1L);
        BookingClientResponse created = client.createBooking(bookingRequest);
        BookingClientResponse updated = client.updateBooking(1L, bookingRequest);
        PreOrderClientResponse createdPreOrder = client.createPreOrder(preOrderRequest);
        PreOrderClientResponse updatedPreOrder = client.updatePreOrder(2L, preOrderRequest);
        client.deleteBooking(1L);
        client.deletePreOrder(2L);

        assertThat(found.getBookingId()).isEqualTo(1L);
        assertThat(created.getTable().getTableNumber()).isEqualTo("T3");
        assertThat(updated.getStatus()).isEqualTo("CONFIRMED");
        assertThat(createdPreOrder.getPreOrderId()).isEqualTo(2L);
        assertThat(updatedPreOrder.getItems()).hasSize(1);
    }

    @Test
    void menuDomainClientReturnsMenuItemsAndMapsHttpErrors() {
        MenuDomainClientImpl client = menuClient(baseUrl);

        MenuItemClientResponse item = client.getMenuItemById(10L);

        assertThat(item.getMenuItemId()).isEqualTo(10L);
        assertThat(item.isAvailable()).isTrue();
        assertThatThrownBy(() -> client.getMenuItemById(404L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("menu-service resource not found");
        assertThatThrownBy(() -> client.getMenuItemById(400L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("menu-service rejected");
        assertThatThrownBy(() -> client.getMenuItemById(500L))
                .isInstanceOf(DownstreamServiceException.class)
                .hasMessageContaining("HTTP 500");
    }

    @Test
    void reservationDomainClientMapsErrorsFromReadUpdateAndDeleteRequests() {
        ReservationDomainClientImpl client = reservationClient(baseUrl);
        CreateBookingClientRequest bookingRequest = CreateBookingClientRequest.builder()
                .customerId(101L)
                .tableId(3L)
                .reservationDate(LocalDate.now().plusDays(1))
                .timeSlotStart(LocalTime.of(18, 0))
                .timeSlotEnd(LocalTime.of(20, 0))
                .partySize(4)
                .status("CONFIRMED")
                .build();
        CreatePreOrderClientRequest preOrderRequest = CreatePreOrderClientRequest.builder()
                .bookingId(1L)
                .items(java.util.List.of())
                .build();

        assertThatThrownBy(() -> client.getBookingById(404L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> client.updateBooking(404L, bookingRequest)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> client.updatePreOrder(404L, preOrderRequest)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> client.deleteBooking(404L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> client.deletePreOrder(404L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void loyaltyDomainClientReturnsOptionalAccountsAndEarnedPoints() {
        LoyaltyDomainClientImpl client = loyaltyClient(baseUrl);

        Optional<LoyaltyAccountClientResponse> existing = client.getLoyaltyAccountByCustomerId(101L);
        Optional<LoyaltyAccountClientResponse> missing = client.getLoyaltyAccountByCustomerId(999L);
        Optional<LoyaltyAccountClientResponse> earned = client.earnPoints(101L, 1L, 125);
        Optional<LoyaltyAccountClientResponse> missingOnEarn = client.earnPoints(999L, 1L, 125);

        assertThat(existing).isPresent();
        assertThat(existing.get().getTier()).isEqualTo("SILVER");
        assertThat(missing).isEmpty();
        assertThat(earned).isPresent();
        assertThat(earned.get().getPointsBalance()).isEqualTo(1625);
        assertThat(missingOnEarn).isEmpty();
    }

    @Test
    void requestFailuresBecomeServiceUnavailableDownstreamExceptions() {
        MenuDomainClientImpl client = menuClient("http://localhost:1");

        assertThatThrownBy(() -> client.getMenuItemById(10L))
                .isInstanceOf(DownstreamServiceException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    private ReservationDomainClientImpl reservationClient(String url) {
        ReservationDomainClientImpl client = new ReservationDomainClientImpl(WebClient.builder());
        ReflectionTestUtils.setField(client, "reservationBaseUrl", url);
        return client;
    }

    private MenuDomainClientImpl menuClient(String url) {
        MenuDomainClientImpl client = new MenuDomainClientImpl(WebClient.builder());
        ReflectionTestUtils.setField(client, "menuBaseUrl", url);
        return client;
    }

    private LoyaltyDomainClientImpl loyaltyClient(String url) {
        LoyaltyDomainClientImpl client = new LoyaltyDomainClientImpl(WebClient.builder());
        ReflectionTestUtils.setField(client, "loyaltyBaseUrl", url);
        return client;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String key = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
        switch (key) {
            case "GET /api/v1/bookings/1", "POST /api/v1/bookings", "PUT /api/v1/bookings/1" ->
                    respond(exchange, 200, bookingJson());
            case "DELETE /api/v1/bookings/1", "DELETE /api/v1/pre-orders/2" ->
                    respond(exchange, 204, "");
            case "POST /api/v1/pre-orders", "PUT /api/v1/pre-orders/2" ->
                    respond(exchange, 200, preOrderJson());
            case "GET /api/v1/menu-items/10" ->
                    respond(exchange, 200, menuItemJson());
            case "GET /api/v1/menu-items/400" ->
                    respond(exchange, 400, "{\"message\":\"bad menu item\"}");
            case "GET /api/v1/menu-items/500" ->
                    respond(exchange, 500, "{\"message\":\"menu is down\"}");
            case "GET /api/v1/menu-items/404" ->
                    respond(exchange, 404, "{\"message\":\"missing menu item\"}");
            case "GET /api/v1/loyalty-accounts/customer/101" ->
                    respond(exchange, 200, loyaltyJson(1500));
            case "POST /api/v1/loyalty-accounts/customer/101/points" ->
                    respond(exchange, 200, loyaltyJson(1625));
            case "GET /api/v1/loyalty-accounts/customer/999", "POST /api/v1/loyalty-accounts/customer/999/points" ->
                    respond(exchange, 404, "{\"message\":\"missing loyalty account\"}");
            default -> respond(exchange, 404, "{\"message\":\"unmapped " + key + "\"}");
        }
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        if (status != 204) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
        } else {
            exchange.sendResponseHeaders(status, -1);
        }
        exchange.close();
    }

    private String bookingJson() {
        return """
                {
                  "bookingId": 1,
                  "customerId": 101,
                  "tableId": 3,
                  "preOrderId": 2,
                  "reservationDate": "2031-06-10",
                  "timeSlotStart": "18:00:00",
                  "timeSlotEnd": "20:00:00",
                  "partySize": 4,
                  "status": "CONFIRMED",
                  "loyaltyPointsEarned": 0,
                  "createdAt": "2031-06-01T10:00:00",
                  "table": {
                    "tableId": 3,
                    "tableNumber": "T3",
                    "seatingCapacity": 4,
                    "tableType": "INDOOR",
                    "status": "AVAILABLE",
                    "sectionName": "Main"
                  }
                }
                """;
    }

    private String preOrderJson() {
        return """
                {
                  "preOrderId": 2,
                  "bookingId": 1,
                  "totalAmount": 24.00,
                  "currency": "CAD",
                  "status": "SUBMITTED",
                  "items": [
                    {
                      "lineItemId": 99,
                      "menuItemId": 10,
                      "quantity": 2,
                      "unitAmount": 12.00,
                      "lineTotal": 24.00,
                      "currency": "CAD"
                    }
                  ]
                }
                """;
    }

    private String menuItemJson() {
        return """
                {
                  "menuItemId": 10,
                  "menuId": 1,
                  "name": "Soup",
                  "description": "Tomato",
                  "amount": 12.00,
                  "currency": "CAD",
                  "category": "APPETIZER",
                  "available": true,
                  "dietaryTags": "VEGETARIAN"
                }
                """;
    }

    private String loyaltyJson(int pointsBalance) {
        return """
                {
                  "accountId": 20,
                  "customerId": 101,
                  "pointsBalance": %d,
                  "tier": "SILVER"
                }
                """.formatted(pointsBalance);
    }
}
