package com.example.restaurantreservation.aggregator;

import com.example.restaurantreservation.aggregator.dataaccesslayer.ReservationAggregateRepository;
import com.example.restaurantreservation.aggregator.domainclientlayer.LoyaltyDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.MenuDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.ReservationDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.BookingClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.DiningTableClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.LoyaltyAccountClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.MenuItemClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.PreOrderClientResponse;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.CreateReservationRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.PreOrderItemRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
class ReservationAggregatorControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private ReservationAggregateRepository reservationAggregateRepository;

    @MockitoBean
    private ReservationDomainClient reservationDomainClient;

    @MockitoBean
    private MenuDomainClient menuDomainClient;

    @MockitoBean
    private LoyaltyDomainClient loyaltyDomainClient;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        reservationAggregateRepository.deleteAll();
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Test
    void createReservationAggregateReturnsCreated() {
        stubSuccessfulClients();

        webTestClient.post()
                .uri("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.bookingId").isEqualTo(10)
                .jsonPath("$.preOrderId").isEqualTo(20)
                .jsonPath("$.totalAmount").isEqualTo(29.00)
                .jsonPath("$.preOrderItems[0].name").isEqualTo("Caesar Salad");
    }

    @Test
    void createReservationAggregateWithUnavailableMenuItemReturnsConflict() {
        MenuItemClientResponse unavailable = menuItem(false);
        when(menuDomainClient.getMenuItemById(1L)).thenReturn(unavailable);

        webTestClient.post()
                .uri("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Menu Item Unavailable");
    }

    @Test
    void getMissingReservationAggregateReturnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/reservations/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").value(message -> ((String) message).contains("missing"));
    }

    private void stubSuccessfulClients() {
        when(menuDomainClient.getMenuItemById(1L)).thenReturn(menuItem(true));
        when(reservationDomainClient.createBooking(any())).thenReturn(booking(10L, null));
        when(reservationDomainClient.createPreOrder(any())).thenReturn(preOrder());
        when(reservationDomainClient.getBookingById(10L)).thenReturn(booking(10L, 20L));
        when(loyaltyDomainClient.getLoyaltyAccountByCustomerId(101L)).thenReturn(Optional.of(loyaltyAccount()));
    }

    private CreateReservationRequestDTO request() {
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        request.setCustomerId(101L);
        request.setTableId(2L);
        request.setReservationDate(LocalDate.now().plusDays(30));
        request.setTimeSlotStart(LocalTime.of(18, 0));
        request.setTimeSlotEnd(LocalTime.of(20, 0));
        request.setPartySize(2);
        request.setStatus("PENDING");
        PreOrderItemRequestDTO item = new PreOrderItemRequestDTO();
        item.setMenuItemId(1L);
        item.setQuantity(2);
        request.setPreOrderItems(List.of(item));
        return request;
    }

    private MenuItemClientResponse menuItem(boolean available) {
        MenuItemClientResponse menuItem = new MenuItemClientResponse();
        menuItem.setMenuItemId(1L);
        menuItem.setName("Caesar Salad");
        menuItem.setAmount(new BigDecimal("14.50"));
        menuItem.setCurrency("CAD");
        menuItem.setCategory("APPETIZER");
        menuItem.setAvailable(available);
        return menuItem;
    }

    private BookingClientResponse booking(Long bookingId, Long preOrderId) {
        BookingClientResponse booking = new BookingClientResponse();
        booking.setBookingId(bookingId);
        booking.setPreOrderId(preOrderId);
        booking.setCustomerId(101L);
        booking.setTableId(2L);
        booking.setStatus("PENDING");
        booking.setTable(table());
        return booking;
    }

    private DiningTableClientResponse table() {
        DiningTableClientResponse table = new DiningTableClientResponse();
        table.setTableId(2L);
        table.setTableNumber("T02");
        table.setSeatingCapacity(4);
        table.setTableType("INDOOR");
        table.setStatus("RESERVED");
        table.setSectionName("Main Dining Room");
        return table;
    }

    private PreOrderClientResponse preOrder() {
        PreOrderClientResponse preOrder = new PreOrderClientResponse();
        preOrder.setPreOrderId(20L);
        preOrder.setBookingId(10L);
        preOrder.setTotalAmount(new BigDecimal("29.00"));
        preOrder.setCurrency("CAD");
        preOrder.setStatus("DRAFT");
        return preOrder;
    }

    private LoyaltyAccountClientResponse loyaltyAccount() {
        LoyaltyAccountClientResponse loyaltyAccount = new LoyaltyAccountClientResponse();
        loyaltyAccount.setAccountId(1L);
        loyaltyAccount.setCustomerId(101L);
        loyaltyAccount.setPointsBalance(1500);
        loyaltyAccount.setTier("SILVER");
        return loyaltyAccount;
    }
}
