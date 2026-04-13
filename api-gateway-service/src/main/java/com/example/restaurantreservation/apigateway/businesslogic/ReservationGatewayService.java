package com.example.restaurantreservation.apigateway.businesslogic;

import com.example.restaurantreservation.apigateway.presentation.dto.booking.BookingResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreateBookingRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.PreOrderSummaryDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.DiningTableResponseDTO;

import java.util.List;

public interface ReservationGatewayService {

    List<BookingResponseDTO> getAllBookings();
    BookingResponseDTO getBookingById(Long id);
    BookingResponseDTO createBooking(CreateBookingRequestDTO request);
    BookingResponseDTO updateBooking(Long id, CreateBookingRequestDTO request);
    void deleteBooking(Long id);

    PreOrderSummaryDTO getPreOrderById(Long id);
    PreOrderSummaryDTO createPreOrder(CreatePreOrderRequestDTO request);
    PreOrderSummaryDTO updatePreOrder(Long id, CreatePreOrderRequestDTO request);
    void deletePreOrder(Long id);

    List<DiningTableResponseDTO> getAllDiningTables();
    DiningTableResponseDTO getDiningTableById(Long id);
    DiningTableResponseDTO createDiningTable(CreateDiningTableRequestDTO request);
    DiningTableResponseDTO updateDiningTable(Long id, CreateDiningTableRequestDTO request);
    void deleteDiningTable(Long id);
}
