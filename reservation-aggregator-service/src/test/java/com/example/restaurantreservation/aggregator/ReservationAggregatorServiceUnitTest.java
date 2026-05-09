package com.example.restaurantreservation.aggregator;

import com.example.restaurantreservation.aggregator.businesslogiclayer.ReservationAggregatorServiceImpl;
import com.example.restaurantreservation.aggregator.dataaccesslayer.ReservationAggregateRepository;
import com.example.restaurantreservation.aggregator.datamappinglayer.ReservationAggregateMapper;
import com.example.restaurantreservation.aggregator.domain.ReservationAggregate;
import com.example.restaurantreservation.aggregator.domainclientlayer.LoyaltyDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.MenuDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.ReservationDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.BookingClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.DiningTableClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.LoyaltyAccountClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.MenuItemClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.PreOrderClientResponse;
import com.example.restaurantreservation.aggregator.exception.InvalidInputException;
import com.example.restaurantreservation.aggregator.exception.MenuItemUnavailableException;
import com.example.restaurantreservation.aggregator.exception.NotFoundException;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.CreateReservationRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.PreOrderItemRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.ReservationAggregateResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationAggregatorServiceUnitTest {

    @Mock
    private ReservationAggregateRepository reservationAggregateRepository;

    @Mock
    private ReservationDomainClient reservationDomainClient;

    @Mock
    private MenuDomainClient menuDomainClient;

    @Mock
    private LoyaltyDomainClient loyaltyDomainClient;

    @InjectMocks
    private ReservationAggregatorServiceImpl reservationAggregatorService;

    @Test
    void createComputesPreOrderTotalFromMenuPrices() {
        reservationAggregatorService = new ReservationAggregatorServiceImpl(
                reservationAggregateRepository,
                reservationDomainClient,
                menuDomainClient,
                loyaltyDomainClient,
                new ReservationAggregateMapper());
        CreateReservationRequestDTO request = reservationRequest("PENDING");
        when(menuDomainClient.getMenuItemById(1L)).thenReturn(menuItem(1L, "Caesar Salad", "14.50", true));
        when(menuDomainClient.getMenuItemById(2L)).thenReturn(menuItem(2L, "Soup", "12.00", true));
        when(reservationDomainClient.createBooking(any())).thenReturn(booking(10L, null, "PENDING"));
        when(reservationDomainClient.createPreOrder(any())).thenReturn(preOrder(20L, 10L, "41.00"));
        when(reservationDomainClient.getBookingById(10L)).thenReturn(booking(10L, 20L, "PENDING"));
        when(loyaltyDomainClient.getLoyaltyAccountByCustomerId(101L)).thenReturn(Optional.of(loyaltyAccount()));
        when(reservationAggregateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationAggregateResponseDTO response = reservationAggregatorService.create(request);

        assertThat(response.getTotalAmount()).isEqualByComparingTo("41.00");
        assertThat(response.getPreOrderItems()).hasSize(2);
        assertThat(response.getLoyaltyAccount().getTier()).isEqualTo("SILVER");
        ArgumentCaptor<ReservationAggregate> aggregateCaptor = ArgumentCaptor.forClass(ReservationAggregate.class);
        verify(reservationAggregateRepository).save(aggregateCaptor.capture());
        assertThat(aggregateCaptor.getValue().getPreOrderItems())
                .extracting("lineTotal")
                .containsExactly(new BigDecimal("29.00"), new BigDecimal("12.00"));
    }

    @Test
    void createRejectsUnavailableMenuItem() {
        reservationAggregatorService = service();
        CreateReservationRequestDTO request = reservationRequest("PENDING");
        when(menuDomainClient.getMenuItemById(1L)).thenReturn(menuItem(1L, "Caesar Salad", "14.50", false));

        assertThatThrownBy(() -> reservationAggregatorService.create(request))
                .isInstanceOf(MenuItemUnavailableException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void updateAwardsLoyaltyPointsOnceWhenBookingCompletes() {
        reservationAggregatorService = service();
        CreateReservationRequestDTO request = reservationRequest("COMPLETED");
        ReservationAggregate existing = ReservationAggregate.builder()
                .aggregateId("agg-1")
                .bookingId(10L)
                .preOrderId(20L)
                .customerId(101L)
                .loyaltyPointsEarned(0)
                .build();
        when(reservationAggregateRepository.findById("agg-1")).thenReturn(Optional.of(existing));
        when(menuDomainClient.getMenuItemById(1L)).thenReturn(menuItem(1L, "Caesar Salad", "14.50", true));
        when(menuDomainClient.getMenuItemById(2L)).thenReturn(menuItem(2L, "Soup", "12.00", true));
        when(reservationDomainClient.updateBooking(any(), any())).thenReturn(booking(10L, 20L, "COMPLETED"));
        when(reservationDomainClient.updatePreOrder(any(), any())).thenReturn(preOrder(20L, 10L, "41.00"));
        when(loyaltyDomainClient.getLoyaltyAccountByCustomerId(101L)).thenReturn(Optional.of(loyaltyAccount()));
        when(loyaltyDomainClient.earnPoints(101L, 10L, 100)).thenReturn(Optional.of(loyaltyAccount(1600)));
        when(reservationAggregateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationAggregateResponseDTO response = reservationAggregatorService.update("agg-1", request);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getLoyaltyAccount().getPointsBalance()).isEqualTo(1600);
        verify(loyaltyDomainClient).earnPoints(101L, 10L, 100);
    }

    @Test
    void updateRejectsMissingAggregate() {
        reservationAggregatorService = service();
        when(reservationAggregateRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationAggregatorService.update("missing", reservationRequest("PENDING")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createRejectsInvalidTimeSlot() {
        reservationAggregatorService = service();
        CreateReservationRequestDTO request = reservationRequest("PENDING");
        request.setTimeSlotStart(LocalTime.of(20, 0));
        request.setTimeSlotEnd(LocalTime.of(18, 0));

        assertThatThrownBy(() -> reservationAggregatorService.create(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("after start");
    }

    @Test
    void deleteRemovesDownstreamResourcesAndAggregate() {
        reservationAggregatorService = service();
        ReservationAggregate aggregate = ReservationAggregate.builder()
                .aggregateId("agg-1")
                .bookingId(10L)
                .preOrderId(20L)
                .build();
        when(reservationAggregateRepository.findById("agg-1")).thenReturn(Optional.of(aggregate));

        reservationAggregatorService.delete("agg-1");

        verify(reservationDomainClient).deletePreOrder(20L);
        verify(reservationDomainClient).deleteBooking(10L);
        verify(reservationAggregateRepository).deleteById("agg-1");
    }

    private ReservationAggregatorServiceImpl service() {
        return new ReservationAggregatorServiceImpl(
                reservationAggregateRepository,
                reservationDomainClient,
                menuDomainClient,
                loyaltyDomainClient,
                new ReservationAggregateMapper());
    }

    private CreateReservationRequestDTO reservationRequest(String status) {
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        request.setCustomerId(101L);
        request.setTableId(2L);
        request.setReservationDate(LocalDate.now().plusDays(30));
        request.setTimeSlotStart(LocalTime.of(18, 0));
        request.setTimeSlotEnd(LocalTime.of(20, 0));
        request.setPartySize(2);
        request.setStatus(status);

        PreOrderItemRequestDTO first = new PreOrderItemRequestDTO();
        first.setMenuItemId(1L);
        first.setQuantity(2);
        PreOrderItemRequestDTO second = new PreOrderItemRequestDTO();
        second.setMenuItemId(2L);
        second.setQuantity(1);
        request.setPreOrderItems(List.of(first, second));
        return request;
    }

    private MenuItemClientResponse menuItem(Long id, String name, String amount, boolean available) {
        MenuItemClientResponse menuItem = new MenuItemClientResponse();
        menuItem.setMenuItemId(id);
        menuItem.setName(name);
        menuItem.setAmount(new BigDecimal(amount));
        menuItem.setCurrency("CAD");
        menuItem.setCategory("APPETIZER");
        menuItem.setAvailable(available);
        return menuItem;
    }

    private BookingClientResponse booking(Long bookingId, Long preOrderId, String status) {
        BookingClientResponse booking = new BookingClientResponse();
        booking.setBookingId(bookingId);
        booking.setPreOrderId(preOrderId);
        booking.setCustomerId(101L);
        booking.setTableId(2L);
        booking.setStatus(status);
        booking.setLoyaltyPointsEarned("COMPLETED".equals(status) ? 100 : 0);
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

    private PreOrderClientResponse preOrder(Long preOrderId, Long bookingId, String total) {
        PreOrderClientResponse preOrder = new PreOrderClientResponse();
        preOrder.setPreOrderId(preOrderId);
        preOrder.setBookingId(bookingId);
        preOrder.setTotalAmount(new BigDecimal(total));
        preOrder.setCurrency("CAD");
        preOrder.setStatus("DRAFT");
        return preOrder;
    }

    private LoyaltyAccountClientResponse loyaltyAccount() {
        return loyaltyAccount(1500);
    }

    private LoyaltyAccountClientResponse loyaltyAccount(int points) {
        LoyaltyAccountClientResponse loyaltyAccount = new LoyaltyAccountClientResponse();
        loyaltyAccount.setAccountId(1L);
        loyaltyAccount.setCustomerId(101L);
        loyaltyAccount.setPointsBalance(points);
        loyaltyAccount.setTier("SILVER");
        return loyaltyAccount;
    }
}
