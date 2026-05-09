package com.example.restaurantreservation.apigateway;

import com.example.restaurantreservation.apigateway.businesslogic.AggregatorGatewayService;
import com.example.restaurantreservation.apigateway.businesslogic.LoyaltyGatewayService;
import com.example.restaurantreservation.apigateway.businesslogic.MenuGatewayService;
import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayService;
import com.example.restaurantreservation.apigateway.presentation.BookingGatewayController;
import com.example.restaurantreservation.apigateway.presentation.DiningTableGatewayController;
import com.example.restaurantreservation.apigateway.presentation.LoyaltyAccountGatewayController;
import com.example.restaurantreservation.apigateway.presentation.MenuItemGatewayController;
import com.example.restaurantreservation.apigateway.presentation.PreOrderGatewayController;
import com.example.restaurantreservation.apigateway.presentation.ReservationAggregatorGatewayController;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.BookingResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.BookingStatus;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreateBookingRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.PreOrderStatus;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.PreOrderSummaryDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.DiningTableResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.TableStatus;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.TableType;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.LoyaltyAccountResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.LoyaltyTier;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuCategory;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuItemResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.CreateReservationRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.PreOrderItemRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.ReservationAggregateResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiGatewayControllerUnitTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void reservationAggregatorControllerSupportsCrudAndAddsLinks() {
        AggregatorGatewayService service = mock(AggregatorGatewayService.class);
        ReservationAggregatorGatewayController controller = new ReservationAggregatorGatewayController(service);
        ReservationAggregateResponseDTO aggregate = aggregate();
        CreateReservationRequestDTO request = reservationRequest();
        setRequest("GET", "/api/v1/reservations");

        when(service.getAllReservations()).thenReturn(List.of(aggregate()));
        when(service.getReservationById("agg-1")).thenReturn(aggregate());
        when(service.createReservation(request)).thenReturn(aggregate());
        when(service.updateReservation("agg-1", request)).thenReturn(aggregate());

        assertThat(controller.getAll().getBody()).hasSize(1);
        assertThat(controller.getById("agg-1").getBody().get_links()).containsKeys("self", "booking", "table", "pre-order");

        setRequest("POST", "/api/v1/reservations");
        assertThat(controller.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(controller.update("agg-1", request).getBody().getAggregateId()).isEqualTo(aggregate.getAggregateId());
        assertThat(controller.delete("agg-1").getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteReservation("agg-1");
    }

    @Test
    void bookingGatewayControllerSupportsCrudAndAddsLinks() {
        ReservationGatewayService service = mock(ReservationGatewayService.class);
        BookingGatewayController controller = new BookingGatewayController(service);
        CreateBookingRequestDTO request = bookingRequest();
        setRequest("GET", "/api/v1/bookings");

        when(service.getAllBookings()).thenReturn(List.of(booking()));
        when(service.getBookingById(1L)).thenReturn(booking());
        when(service.createBooking(request)).thenReturn(booking());
        when(service.updateBooking(1L, request)).thenReturn(booking());

        assertThat(controller.getAll().getBody()).hasSize(1);
        assertThat(controller.getById(1L).getBody().get_links()).containsKeys("self", "table", "pre-order");

        setRequest("POST", "/api/v1/bookings");
        assertThat(controller.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(controller.update(1L, request).getBody().getBookingId()).isEqualTo(1L);
        assertThat(controller.delete(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteBooking(1L);
    }

    @Test
    void diningTableGatewayControllerSupportsCrudAndAddsLinks() {
        ReservationGatewayService service = mock(ReservationGatewayService.class);
        DiningTableGatewayController controller = new DiningTableGatewayController(service);
        CreateDiningTableRequestDTO request = tableRequest();
        setRequest("GET", "/api/v1/dining-tables");

        when(service.getAllDiningTables()).thenReturn(List.of(table()));
        when(service.getDiningTableById(3L)).thenReturn(table());
        when(service.createDiningTable(request)).thenReturn(table());
        when(service.updateDiningTable(3L, request)).thenReturn(table());

        assertThat(controller.getAll().getBody()).hasSize(1);
        assertThat(controller.getById(3L).getBody().get_links()).containsKeys("self", "all-tables");

        setRequest("POST", "/api/v1/dining-tables");
        assertThat(controller.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(controller.update(3L, request).getBody().getTableId()).isEqualTo(3L);
        assertThat(controller.delete(3L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteDiningTable(3L);
    }

    @Test
    void menuItemGatewayControllerSupportsCrudAndAddsLinks() {
        MenuGatewayService service = mock(MenuGatewayService.class);
        MenuItemGatewayController controller = new MenuItemGatewayController(service);
        CreateMenuItemRequestDTO request = menuRequest();
        setRequest("GET", "/api/v1/menu-items");

        when(service.getAllMenuItems()).thenReturn(List.of(menuItem()));
        when(service.getMenuItemById(10L)).thenReturn(menuItem());
        when(service.createMenuItem(request)).thenReturn(menuItem());
        when(service.updateMenuItem(10L, request)).thenReturn(menuItem());

        assertThat(controller.getAll().getBody()).hasSize(1);
        assertThat(controller.getById(10L).getBody().get_links()).containsKeys("self", "all-items");

        setRequest("POST", "/api/v1/menu-items");
        assertThat(controller.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(controller.update(10L, request).getBody().getMenuItemId()).isEqualTo(10L);
        assertThat(controller.delete(10L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteMenuItem(10L);
    }

    @Test
    void loyaltyGatewayControllerSupportsCrudAndAddsLinks() {
        LoyaltyGatewayService service = mock(LoyaltyGatewayService.class);
        LoyaltyAccountGatewayController controller = new LoyaltyAccountGatewayController(service);
        CreateLoyaltyAccountRequestDTO request = loyaltyRequest();
        setRequest("GET", "/api/v1/loyalty-accounts");

        when(service.getAllLoyaltyAccounts()).thenReturn(List.of(loyaltyAccount()));
        when(service.getLoyaltyAccountById(20L)).thenReturn(loyaltyAccount());
        when(service.createLoyaltyAccount(request)).thenReturn(loyaltyAccount());
        when(service.updateLoyaltyAccount(20L, request)).thenReturn(loyaltyAccount());

        assertThat(controller.getAll().getBody()).hasSize(1);
        assertThat(controller.getById(20L).getBody().get_links()).containsKeys("self", "all-accounts");

        setRequest("POST", "/api/v1/loyalty-accounts");
        assertThat(controller.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(controller.update(20L, request).getBody().getAccountId()).isEqualTo(20L);
        assertThat(controller.delete(20L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteLoyaltyAccount(20L);
    }

    @Test
    void preOrderGatewayControllerSupportsCrudAndAddsLinks() {
        ReservationGatewayService service = mock(ReservationGatewayService.class);
        PreOrderGatewayController controller = new PreOrderGatewayController(service);
        CreatePreOrderRequestDTO request = preOrderRequest();
        setRequest("GET", "/api/v1/pre-orders/2");

        when(service.getPreOrderById(2L)).thenReturn(preOrder());
        when(service.createPreOrder(request)).thenReturn(preOrder());
        when(service.updatePreOrder(2L, request)).thenReturn(preOrder());

        assertThat(controller.getById(2L).getBody().get_links()).containsKeys("self", "booking");

        setRequest("POST", "/api/v1/pre-orders");
        assertThat(controller.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(controller.update(2L, request).getBody().getPreOrderId()).isEqualTo(2L);
        assertThat(controller.delete(2L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deletePreOrder(2L);
    }

    private void setRequest(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private BookingResponseDTO booking() {
        return BookingResponseDTO.builder()
                .bookingId(1L)
                .customerId(101L)
                .tableId(3L)
                .preOrderId(2L)
                .reservationDate(LocalDate.now().plusDays(1))
                .timeSlotStart(LocalTime.of(18, 0))
                .timeSlotEnd(LocalTime.of(20, 0))
                .partySize(4)
                .status(BookingStatus.CONFIRMED)
                .build();
    }

    private CreateBookingRequestDTO bookingRequest() {
        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setCustomerId(101L);
        request.setTableId(3L);
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setTimeSlotStart(LocalTime.of(18, 0));
        request.setTimeSlotEnd(LocalTime.of(20, 0));
        request.setPartySize(4);
        request.setStatus(BookingStatus.PENDING);
        return request;
    }

    private PreOrderSummaryDTO preOrder() {
        return PreOrderSummaryDTO.builder()
                .preOrderId(2L)
                .bookingId(1L)
                .totalAmount(new BigDecimal("24.00"))
                .currency("CAD")
                .status(PreOrderStatus.SUBMITTED)
                .build();
    }

    private CreatePreOrderRequestDTO preOrderRequest() {
        CreatePreOrderRequestDTO request = new CreatePreOrderRequestDTO();
        request.setBookingId(1L);
        CreatePreOrderRequestDTO.LineItemRequest item = new CreatePreOrderRequestDTO.LineItemRequest();
        item.setMenuItemId(10L);
        item.setQuantity(2);
        item.setUnitAmount(new BigDecimal("12.00"));
        item.setCurrency("CAD");
        request.setItems(List.of(item));
        return request;
    }

    private DiningTableResponseDTO table() {
        return DiningTableResponseDTO.builder()
                .tableId(3L)
                .tableNumber("T3")
                .seatingCapacity(4)
                .tableType(TableType.INDOOR)
                .status(TableStatus.AVAILABLE)
                .sectionId(1L)
                .sectionName("Main")
                .build();
    }

    private CreateDiningTableRequestDTO tableRequest() {
        CreateDiningTableRequestDTO request = new CreateDiningTableRequestDTO();
        request.setTableNumber("T3");
        request.setSeatingCapacity(4);
        request.setTableType(TableType.INDOOR);
        request.setStatus(TableStatus.AVAILABLE);
        request.setSectionId(1L);
        return request;
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

    private CreateMenuItemRequestDTO menuRequest() {
        CreateMenuItemRequestDTO request = new CreateMenuItemRequestDTO();
        request.setMenuId(1L);
        request.setName("Soup");
        request.setDescription("Tomato");
        request.setAmount(new BigDecimal("12.00"));
        request.setCurrency("CAD");
        request.setCategory(MenuCategory.APPETIZER);
        request.setAvailable(true);
        return request;
    }

    private LoyaltyAccountResponseDTO loyaltyAccount() {
        return LoyaltyAccountResponseDTO.builder()
                .accountId(20L)
                .customerId(101L)
                .pointsBalance(50)
                .tier(LoyaltyTier.BRONZE)
                .enrollmentDate(LocalDate.now())
                .build();
    }

    private CreateLoyaltyAccountRequestDTO loyaltyRequest() {
        CreateLoyaltyAccountRequestDTO request = new CreateLoyaltyAccountRequestDTO();
        request.setCustomerId(101L);
        request.setPointsBalance(50);
        request.setTier(LoyaltyTier.BRONZE);
        request.setEnrollmentDate(LocalDate.now());
        return request;
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

    private CreateReservationRequestDTO reservationRequest() {
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        request.setCustomerId(101L);
        request.setTableId(3L);
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setTimeSlotStart(LocalTime.of(18, 0));
        request.setTimeSlotEnd(LocalTime.of(20, 0));
        request.setPartySize(4);
        PreOrderItemRequestDTO item = new PreOrderItemRequestDTO();
        item.setMenuItemId(10L);
        item.setQuantity(2);
        request.setPreOrderItems(List.of(item));
        return request;
    }
}
