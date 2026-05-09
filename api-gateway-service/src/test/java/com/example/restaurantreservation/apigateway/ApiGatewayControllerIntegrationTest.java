package com.example.restaurantreservation.apigateway;

import com.example.restaurantreservation.apigateway.businesslogic.AggregatorGatewayService;
import com.example.restaurantreservation.apigateway.businesslogic.LoyaltyGatewayService;
import com.example.restaurantreservation.apigateway.businesslogic.MenuGatewayService;
import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuCategory;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuItemResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.ReservationAggregateResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @MockitoBean
    private AggregatorGatewayService aggregatorGatewayService;

    @MockitoBean
    private ReservationGatewayService reservationGatewayService;

    @MockitoBean
    private MenuGatewayService menuGatewayService;

    @MockitoBean
    private LoyaltyGatewayService loyaltyGatewayService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Test
    void getReservationAggregateAddsGatewayHateoasLinks() {
        when(aggregatorGatewayService.getReservationById("agg-1")).thenReturn(aggregate());

        webTestClient.get()
                .uri("/api/v1/reservations/agg-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aggregateId").isEqualTo("agg-1")
                .jsonPath("$._links.self.href").value(href -> href.toString().contains("/api/v1/reservations/agg-1"))
                .jsonPath("$._links.booking.href").value(href -> href.toString().contains("/api/v1/bookings/1"))
                .jsonPath("$._links.pre-order.href").value(href -> href.toString().contains("/api/v1/pre-orders/2"));
    }

    @Test
    void createReservationAggregateReturnsCreatedLocation() {
        when(aggregatorGatewayService.createReservation(any())).thenReturn(aggregate());

        webTestClient.post()
                .uri("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "customerId": 101,
                          "tableId": 3,
                          "reservationDate": "%s",
                          "timeSlotStart": "18:00:00",
                          "timeSlotEnd": "20:00:00",
                          "partySize": 4,
                          "preOrderItems": [
                            {"menuItemId": 10, "quantity": 2}
                          ]
                        }
                        """.formatted(LocalDate.now().plusDays(1)))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/api/v1/reservations/agg-1")
                .expectBody()
                .jsonPath("$.aggregateId").isEqualTo("agg-1")
                .jsonPath("$._links.table.href").value(href -> href.toString().contains("/api/v1/dining-tables/3"));

        verify(aggregatorGatewayService).createReservation(any());
    }

    @Test
    void downstreamHttpErrorIsReturnedThroughGatewayExceptionHandler() {
        HttpClientErrorException missing = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                "{\"message\":\"reservation aggregate missing\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(aggregatorGatewayService.getReservationById("missing")).thenThrow(missing);

        webTestClient.get()
                .uri("/api/v1/reservations/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(body -> body.contains("reservation aggregate missing"));
    }

    @Test
    void controllerValidationRejectsInvalidMenuPostBeforeCallingDomainClient() {
        webTestClient.post()
                .uri("/api/v1/menu-items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "",
                          "amount": 0,
                          "currency": "CAD",
                          "category": "APPETIZER"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(menuGatewayService);
    }

    @Test
    void getMenuItemsForwardsThroughMockedDomainClientLayer() {
        when(menuGatewayService.getAllMenuItems()).thenReturn(List.of(menuItem()));

        webTestClient.get()
                .uri("/api/v1/menu-items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].menuItemId").isEqualTo(10)
                .jsonPath("$[0]._links.self.href").value(href -> href.toString().contains("/api/v1/menu-items/10"));

        verify(menuGatewayService).getAllMenuItems();
    }

    @Test
    void deleteReservationAggregateReturnsNoContent() {
        webTestClient.delete()
                .uri("/api/v1/reservations/agg-1")
                .exchange()
                .expectStatus().isNoContent();

        verify(aggregatorGatewayService).deleteReservation(eq("agg-1"));
    }

    private ReservationAggregateResponseDTO aggregate() {
        return ReservationAggregateResponseDTO.builder()
                .aggregateId("agg-1")
                .bookingId(1L)
                .preOrderId(2L)
                .customerId(101L)
                .tableId(3L)
                .reservationDate(LocalDate.now().plusDays(1))
                .timeSlotStart(LocalTime.of(18, 0))
                .timeSlotEnd(LocalTime.of(20, 0))
                .partySize(4)
                .status("CONFIRMED")
                .totalAmount(new BigDecimal("24.00"))
                .currency("CAD")
                .build();
    }

    private MenuItemResponseDTO menuItem() {
        return MenuItemResponseDTO.builder()
                .menuItemId(10L)
                .menuId(1L)
                .name("Soup")
                .description("Tomato")
                .amount(new BigDecimal("12.00"))
                .currency("CAD")
                .category(MenuCategory.APPETIZER)
                .isAvailable(true)
                .build();
    }
}
