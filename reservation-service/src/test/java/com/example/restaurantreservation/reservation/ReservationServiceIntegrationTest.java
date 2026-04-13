package com.example.restaurantreservation.reservation;

import com.example.restaurantreservation.booking.businesslogiclayer.TableBookingService;
import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreateBookingRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.exception.BusinessRuleViolationException;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.floor.businesslogiclayer.DiningTableService;
import com.example.restaurantreservation.floor.dataaccesslayer.DiningTableRepository;
import com.example.restaurantreservation.floor.domain.DiningTable;
import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.floor.domain.enums.TableType;
import com.example.restaurantreservation.floor.presentationlayer.dto.CreateDiningTableRequestDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("testing")
class ReservationServiceIntegrationTest {

    @Autowired
    private DiningTableService diningTableService;

    @Autowired
    private DiningTableRepository diningTableRepository;

    @Autowired
    private TableBookingService tableBookingService;

    @Test
    void assertReservableRejectsMaintenanceReservedAndOversizedParties() {
        assertThatThrownBy(() -> diningTableService.assertReservable(4L, 2))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("under maintenance");

        DiningTable table = diningTableRepository.findById(1L).orElseThrow();
        table.setStatus(TableStatus.RESERVED);
        diningTableRepository.save(table);

        assertThatThrownBy(() -> diningTableService.assertReservable(1L, 2))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("currently RESERVED");

        assertThatThrownBy(() -> diningTableService.assertReservable(2L, 10))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("exceeds table capacity");
    }

    @Test
    void createDiningTableWithUnknownSectionThrowsNotFound() {
        CreateDiningTableRequestDTO request = new CreateDiningTableRequestDTO();
        request.setTableNumber("T77");
        request.setSeatingCapacity(4);
        request.setTableType(TableType.INDOOR);
        request.setSectionId(999L);

        assertThatThrownBy(() -> diningTableService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("FloorSection");
    }

    @Test
    void createBookingWithInvalidTimeSlotThrowsBusinessRuleViolation() {
        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setCustomerId(501L);
        request.setTableId(1L);
        request.setReservationDate(LocalDate.now().plusDays(20));
        request.setTimeSlotStart(LocalTime.of(19, 0));
        request.setTimeSlotEnd(LocalTime.of(18, 0));
        request.setPartySize(2);

        assertThatThrownBy(() -> tableBookingService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("after start");
    }

    @Test
    void updateBookingToCancelledReleasesTheTable() {
        CreateBookingRequestDTO createRequest = new CreateBookingRequestDTO();
        createRequest.setCustomerId(601L);
        createRequest.setTableId(1L);
        createRequest.setReservationDate(LocalDate.now().plusDays(25));
        createRequest.setTimeSlotStart(LocalTime.of(17, 0));
        createRequest.setTimeSlotEnd(LocalTime.of(19, 0));
        createRequest.setPartySize(2);

        BookingResponseDTO created = tableBookingService.create(createRequest);
        assertThat(diningTableRepository.findById(1L).orElseThrow().getStatus()).isEqualTo(TableStatus.RESERVED);

        CreateBookingRequestDTO updateRequest = new CreateBookingRequestDTO();
        updateRequest.setCustomerId(createRequest.getCustomerId());
        updateRequest.setTableId(createRequest.getTableId());
        updateRequest.setReservationDate(createRequest.getReservationDate());
        updateRequest.setTimeSlotStart(createRequest.getTimeSlotStart());
        updateRequest.setTimeSlotEnd(createRequest.getTimeSlotEnd());
        updateRequest.setPartySize(createRequest.getPartySize());
        updateRequest.setStatus(com.example.restaurantreservation.booking.domain.enums.BookingStatus.CANCELLED);

        BookingResponseDTO updated = tableBookingService.update(created.getBookingId(), updateRequest);

        assertThat(updated.getStatus()).isEqualTo(com.example.restaurantreservation.booking.domain.enums.BookingStatus.CANCELLED);
        assertThat(diningTableRepository.findById(1L).orElseThrow().getStatus()).isEqualTo(TableStatus.AVAILABLE);
    }

    @Test
    void completedBookingCannotAcceptAPreOrder() {
        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setCustomerId(102L);
        request.setTableId(3L);
        request.setReservationDate(LocalDate.of(2030, 5, 21));
        request.setTimeSlotStart(LocalTime.of(19, 0));
        request.setTimeSlotEnd(LocalTime.of(21, 0));
        request.setPartySize(5);
        request.setStatus(com.example.restaurantreservation.booking.domain.enums.BookingStatus.COMPLETED);

        tableBookingService.update(2L, request);

        CreatePreOrderRequestDTO preOrderRequest = new CreatePreOrderRequestDTO();
        preOrderRequest.setBookingId(2L);

        assertThatThrownBy(() -> tableBookingService.createPreOrder(preOrderRequest))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("COMPLETED booking");
    }

    @Test
    void updatePreOrderRejectsInvalidLineItemQuantity() {
        CreatePreOrderRequestDTO request = new CreatePreOrderRequestDTO();
        request.setBookingId(1L);
        CreatePreOrderRequestDTO.LineItemRequest lineItem = new CreatePreOrderRequestDTO.LineItemRequest();
        lineItem.setMenuItemId(1L);
        lineItem.setQuantity(0);
        lineItem.setUnitAmount(new BigDecimal("14.50"));
        request.setItems(List.of(lineItem));

        assertThatThrownBy(() -> tableBookingService.updatePreOrder(1L, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("quantity must be at least 1");
    }

    @Test
    void deletingUnknownPreOrderThrowsNotFound() {
        assertThatThrownBy(() -> tableBookingService.deletePreOrder(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("PreOrder");
    }
}
