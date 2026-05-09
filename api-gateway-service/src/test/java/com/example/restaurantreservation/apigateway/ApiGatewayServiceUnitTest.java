package com.example.restaurantreservation.apigateway;

import com.example.restaurantreservation.apigateway.businesslogic.AggregatorGatewayServiceImpl;
import com.example.restaurantreservation.apigateway.businesslogic.LoyaltyGatewayServiceImpl;
import com.example.restaurantreservation.apigateway.businesslogic.MenuGatewayServiceImpl;
import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayServiceImpl;
import com.example.restaurantreservation.apigateway.presentation.GatewayExceptionHandler;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiGatewayServiceUnitTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    @Test
    void reservationGatewayServiceForwardsBookingPreOrderAndTableRequests() {
        ReservationGatewayServiceImpl service = new ReservationGatewayServiceImpl(restTemplate);
        ReflectionTestUtils.setField(service, "reservationBaseUrl", "http://reservation-service");
        BookingResponseDTO booking = booking();
        PreOrderSummaryDTO preOrder = preOrder();
        DiningTableResponseDTO table = table();
        CreateBookingRequestDTO bookingRequest = bookingRequest();
        CreatePreOrderRequestDTO preOrderRequest = preOrderRequest();
        CreateDiningTableRequestDTO tableRequest = tableRequest();

        when(restTemplate.exchange(eq("http://reservation-service/api/v1/bookings"), eq(HttpMethod.GET),
                isNull(), ArgumentMatchers.<ParameterizedTypeReference<List<BookingResponseDTO>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(booking)));
        when(restTemplate.getForObject("http://reservation-service/api/v1/bookings/1", BookingResponseDTO.class))
                .thenReturn(booking);
        when(restTemplate.postForObject("http://reservation-service/api/v1/bookings", bookingRequest, BookingResponseDTO.class))
                .thenReturn(booking);
        when(restTemplate.exchange(eq("http://reservation-service/api/v1/bookings/1"), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(BookingResponseDTO.class))).thenReturn(ResponseEntity.ok(booking));
        when(restTemplate.getForObject("http://reservation-service/api/v1/pre-orders/2", PreOrderSummaryDTO.class))
                .thenReturn(preOrder);
        when(restTemplate.postForObject("http://reservation-service/api/v1/pre-orders", preOrderRequest, PreOrderSummaryDTO.class))
                .thenReturn(preOrder);
        when(restTemplate.exchange(eq("http://reservation-service/api/v1/pre-orders/2"), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(PreOrderSummaryDTO.class))).thenReturn(ResponseEntity.ok(preOrder));
        when(restTemplate.exchange(eq("http://reservation-service/api/v1/dining-tables"), eq(HttpMethod.GET),
                isNull(), ArgumentMatchers.<ParameterizedTypeReference<List<DiningTableResponseDTO>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(table)));
        when(restTemplate.getForObject("http://reservation-service/api/v1/dining-tables/3", DiningTableResponseDTO.class))
                .thenReturn(table);
        when(restTemplate.postForObject("http://reservation-service/api/v1/dining-tables", tableRequest, DiningTableResponseDTO.class))
                .thenReturn(table);
        when(restTemplate.exchange(eq("http://reservation-service/api/v1/dining-tables/3"), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(DiningTableResponseDTO.class))).thenReturn(ResponseEntity.ok(table));

        assertThat(service.getAllBookings()).containsExactly(booking);
        assertThat(service.getBookingById(1L)).isSameAs(booking);
        assertThat(service.createBooking(bookingRequest)).isSameAs(booking);
        assertThat(service.updateBooking(1L, bookingRequest)).isSameAs(booking);
        service.deleteBooking(1L);
        assertThat(service.getPreOrderById(2L)).isSameAs(preOrder);
        assertThat(service.createPreOrder(preOrderRequest)).isSameAs(preOrder);
        assertThat(service.updatePreOrder(2L, preOrderRequest)).isSameAs(preOrder);
        service.deletePreOrder(2L);
        assertThat(service.getAllDiningTables()).containsExactly(table);
        assertThat(service.getDiningTableById(3L)).isSameAs(table);
        assertThat(service.createDiningTable(tableRequest)).isSameAs(table);
        assertThat(service.updateDiningTable(3L, tableRequest)).isSameAs(table);
        service.deleteDiningTable(3L);

        verify(restTemplate).delete("http://reservation-service/api/v1/bookings/1");
        verify(restTemplate).delete("http://reservation-service/api/v1/pre-orders/2");
        verify(restTemplate).delete("http://reservation-service/api/v1/dining-tables/3");
    }

    @Test
    void menuGatewayServiceForwardsCrudRequests() {
        MenuGatewayServiceImpl service = new MenuGatewayServiceImpl(restTemplate);
        ReflectionTestUtils.setField(service, "menuBaseUrl", "http://menu-service");
        MenuItemResponseDTO item = menuItem();
        CreateMenuItemRequestDTO request = menuRequest();

        when(restTemplate.exchange(eq("http://menu-service/api/v1/menu-items"), eq(HttpMethod.GET),
                isNull(), ArgumentMatchers.<ParameterizedTypeReference<List<MenuItemResponseDTO>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(item)));
        when(restTemplate.getForObject("http://menu-service/api/v1/menu-items/10", MenuItemResponseDTO.class))
                .thenReturn(item);
        when(restTemplate.postForObject("http://menu-service/api/v1/menu-items", request, MenuItemResponseDTO.class))
                .thenReturn(item);
        when(restTemplate.exchange(eq("http://menu-service/api/v1/menu-items/10"), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(MenuItemResponseDTO.class))).thenReturn(ResponseEntity.ok(item));

        assertThat(service.getAllMenuItems()).containsExactly(item);
        assertThat(service.getMenuItemById(10L)).isSameAs(item);
        assertThat(service.createMenuItem(request)).isSameAs(item);
        assertThat(service.updateMenuItem(10L, request)).isSameAs(item);
        service.deleteMenuItem(10L);

        verify(restTemplate).delete("http://menu-service/api/v1/menu-items/10");
    }

    @Test
    void loyaltyGatewayServiceForwardsCrudRequests() {
        LoyaltyGatewayServiceImpl service = new LoyaltyGatewayServiceImpl(restTemplate);
        ReflectionTestUtils.setField(service, "loyaltyBaseUrl", "http://loyalty-service");
        LoyaltyAccountResponseDTO account = loyaltyAccount();
        CreateLoyaltyAccountRequestDTO request = loyaltyRequest();

        when(restTemplate.exchange(eq("http://loyalty-service/api/v1/loyalty-accounts"), eq(HttpMethod.GET),
                isNull(), ArgumentMatchers.<ParameterizedTypeReference<List<LoyaltyAccountResponseDTO>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(account)));
        when(restTemplate.getForObject("http://loyalty-service/api/v1/loyalty-accounts/20", LoyaltyAccountResponseDTO.class))
                .thenReturn(account);
        when(restTemplate.postForObject("http://loyalty-service/api/v1/loyalty-accounts", request, LoyaltyAccountResponseDTO.class))
                .thenReturn(account);
        when(restTemplate.exchange(eq("http://loyalty-service/api/v1/loyalty-accounts/20"), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(LoyaltyAccountResponseDTO.class))).thenReturn(ResponseEntity.ok(account));

        assertThat(service.getAllLoyaltyAccounts()).containsExactly(account);
        assertThat(service.getLoyaltyAccountById(20L)).isSameAs(account);
        assertThat(service.createLoyaltyAccount(request)).isSameAs(account);
        assertThat(service.updateLoyaltyAccount(20L, request)).isSameAs(account);
        service.deleteLoyaltyAccount(20L);

        verify(restTemplate).delete("http://loyalty-service/api/v1/loyalty-accounts/20");
    }

    @Test
    void aggregatorGatewayServiceForwardsReservationAggregateCrudRequests() {
        AggregatorGatewayServiceImpl service = new AggregatorGatewayServiceImpl(restTemplate);
        ReflectionTestUtils.setField(service, "aggregatorBaseUrl", "http://aggregator-service");
        ReservationAggregateResponseDTO response = aggregate();
        CreateReservationRequestDTO request = reservationRequest();

        when(restTemplate.exchange(eq("http://aggregator-service/api/v1/reservations"), eq(HttpMethod.GET),
                isNull(), ArgumentMatchers.<ParameterizedTypeReference<List<ReservationAggregateResponseDTO>>>any()))
                .thenReturn(ResponseEntity.ok(List.of(response)));
        when(restTemplate.getForObject("http://aggregator-service/api/v1/reservations/agg-1", ReservationAggregateResponseDTO.class))
                .thenReturn(response);
        when(restTemplate.postForObject("http://aggregator-service/api/v1/reservations", request, ReservationAggregateResponseDTO.class))
                .thenReturn(response);
        when(restTemplate.exchange(eq("http://aggregator-service/api/v1/reservations/agg-1"), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(ReservationAggregateResponseDTO.class))).thenReturn(ResponseEntity.ok(response));

        assertThat(service.getAllReservations()).containsExactly(response);
        assertThat(service.getReservationById("agg-1")).isSameAs(response);
        assertThat(service.createReservation(request)).isSameAs(response);
        assertThat(service.updateReservation("agg-1", request)).isSameAs(response);
        service.deleteReservation("agg-1");

        verify(restTemplate).delete("http://aggregator-service/api/v1/reservations/agg-1");
    }

    @Test
    void gatewayExceptionHandlerPreservesDownstreamStatusAndUnavailableServiceMessage() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler();
        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                "{\"message\":\"missing\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<String> downstream = handler.handleDownstreamHttpError(notFound);
        ResponseEntity<String> unavailable = handler.handleUnavailableService(new ResourceAccessException("connection refused"));

        assertThat(downstream.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(downstream.getBody()).contains("missing");
        assertThat(unavailable.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(unavailable.getBody()).contains("connection refused");
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
