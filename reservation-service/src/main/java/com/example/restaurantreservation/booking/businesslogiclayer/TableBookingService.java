package com.example.restaurantreservation.booking.businesslogiclayer;

import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreateBookingRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.PreOrderSummaryDTO;

import java.util.List;


public interface TableBookingService {

    List<BookingResponseDTO> findAll();
    BookingResponseDTO findById(Long id);
    BookingResponseDTO create(CreateBookingRequestDTO dto);
    BookingResponseDTO update(Long id, CreateBookingRequestDTO dto);
    void delete(Long id);

    PreOrderSummaryDTO createPreOrder(CreatePreOrderRequestDTO dto);
    PreOrderSummaryDTO getPreOrder(Long preOrderId);
    PreOrderSummaryDTO updatePreOrder(Long preOrderId, CreatePreOrderRequestDTO dto);
    void deletePreOrder(Long preOrderId);
}
