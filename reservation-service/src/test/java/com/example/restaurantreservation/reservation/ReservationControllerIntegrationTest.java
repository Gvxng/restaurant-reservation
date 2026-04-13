package com.example.restaurantreservation.reservation;

import com.example.restaurantreservation.booking.presentationlayer.dto.CreateBookingRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.PreOrderSummaryDTO;
import com.example.restaurantreservation.booking.domain.enums.BookingStatus;
import com.example.restaurantreservation.floor.domain.enums.TableType;
import com.example.restaurantreservation.floor.presentationlayer.dto.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
class ReservationControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUpClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Test
    void getAllBookingsReturnsSeededBookings() {
        webTestClient.get()
                .uri("/api/v1/bookings")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].bookingId").isEqualTo(1);
    }

    @Test
    void getBookingByIdReturnsSeededBooking() {
        webTestClient.get()
                .uri("/api/v1/bookings/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.bookingId").isEqualTo(1)
                .jsonPath("$.table.tableNumber").isEqualTo("T02");
    }

    @Test
    void createBookingReturnsCreated() {
        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setCustomerId(999L);
        request.setTableId(1L);
        request.setReservationDate(LocalDate.now().plusDays(30));
        request.setTimeSlotStart(LocalTime.of(17, 0));
        request.setTimeSlotEnd(LocalTime.of(19, 0));
        request.setPartySize(2);
        request.setStatus(BookingStatus.PENDING);

        BookingResponseDTO created = webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingResponseDTO.class)
                .returnResult()
                .getResponseBody();

        webTestClient.delete()
                .uri("/api/v1/bookings/{id}", created.getBookingId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createBookingWithOverlapReturnsConflict() {
        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setCustomerId(500L);
        request.setTableId(2L);
        request.setReservationDate(LocalDate.of(2030, 5, 20));
        request.setTimeSlotStart(LocalTime.of(18, 30));
        request.setTimeSlotEnd(LocalTime.of(19, 30));
        request.setPartySize(2);
        request.setStatus(BookingStatus.PENDING);

        webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").value(message -> ((String) message).contains("already booked"));
    }

    @Test
    void updateBookingToCompletedReturnsEarnedPoints() {
        Long bookingId = createBookingForTable(1L, LocalDate.now().plusDays(45), LocalTime.of(14, 0), LocalTime.of(16, 0), 2);

        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setCustomerId(701L);
        request.setTableId(1L);
        request.setReservationDate(LocalDate.now().plusDays(45));
        request.setTimeSlotStart(LocalTime.of(14, 0));
        request.setTimeSlotEnd(LocalTime.of(16, 0));
        request.setPartySize(2);
        request.setStatus(BookingStatus.COMPLETED);

        webTestClient.put()
                .uri("/api/v1/bookings/{id}", bookingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.loyaltyPointsEarned").isEqualTo(100);

        webTestClient.delete()
                .uri("/api/v1/bookings/{id}", bookingId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void preOrderEndpointsSupportCrudFlow() {
        CreatePreOrderRequestDTO createRequest = new CreatePreOrderRequestDTO();
        createRequest.setBookingId(2L);
        CreatePreOrderRequestDTO.LineItemRequest lineItem = new CreatePreOrderRequestDTO.LineItemRequest();
        lineItem.setMenuItemId(1L);
        lineItem.setQuantity(2);
        lineItem.setUnitAmount(new BigDecimal("12.50"));
        createRequest.setItems(List.of(lineItem));

        Long preOrderId = webTestClient.post()
                .uri("/api/v1/pre-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PreOrderSummaryDTO.class)
                .returnResult()
                .getResponseBody()
                .getPreOrderId();

        webTestClient.get()
                .uri("/api/v1/pre-orders/{id}", preOrderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1);

        CreatePreOrderRequestDTO updateRequest = new CreatePreOrderRequestDTO();
        updateRequest.setBookingId(2L);
        CreatePreOrderRequestDTO.LineItemRequest updatedItem = new CreatePreOrderRequestDTO.LineItemRequest();
        updatedItem.setMenuItemId(3L);
        updatedItem.setQuantity(1);
        updatedItem.setUnitAmount(new BigDecimal("18.00"));
        updateRequest.setItems(List.of(updatedItem));

        webTestClient.put()
                .uri("/api/v1/pre-orders/{id}", preOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalAmount").isEqualTo(18);

        webTestClient.delete()
                .uri("/api/v1/pre-orders/{id}", preOrderId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/v1/pre-orders/{id}", preOrderId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getDiningTablesReturnsSeededTables() {
        webTestClient.get()
                .uri("/api/v1/dining-tables")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(4)
                .jsonPath("$[1].tableNumber").isEqualTo("T02");
    }

    @Test
    void getDiningTableByIdReturnsSeededTable() {
        webTestClient.get()
                .uri("/api/v1/dining-tables/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tableId").isEqualTo(1)
                .jsonPath("$.tableNumber").isEqualTo("T01");
    }

    @Test
    void diningTableEndpointsSupportCrudFlow() {
        CreateDiningTableRequestDTO request = new CreateDiningTableRequestDTO();
        request.setTableNumber("T99");
        request.setSeatingCapacity(4);
        request.setTableType(TableType.INDOOR);
        request.setSectionId(1L);

        DiningTableResponseDTO created = webTestClient.post()
                .uri("/api/v1/dining-tables")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DiningTableResponseDTO.class)
                .returnResult()
                .getResponseBody();

        request.setSeatingCapacity(6);

        webTestClient.put()
                .uri("/api/v1/dining-tables/{id}", created.getTableId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.seatingCapacity").isEqualTo(6);

        webTestClient.delete()
                .uri("/api/v1/dining-tables/{id}", created.getTableId())
                .exchange()
                .expectStatus().isNoContent();
    }

    private Long createBookingForTable(Long tableId, LocalDate reservationDate, LocalTime start, LocalTime end, int partySize) {
        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setCustomerId(701L);
        request.setTableId(tableId);
        request.setReservationDate(reservationDate);
        request.setTimeSlotStart(start);
        request.setTimeSlotEnd(end);
        request.setPartySize(partySize);
        request.setStatus(BookingStatus.PENDING);

        BookingResponseDTO created = webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingResponseDTO.class)
                .returnResult()
                .getResponseBody();

        return created.getBookingId();
    }
}
