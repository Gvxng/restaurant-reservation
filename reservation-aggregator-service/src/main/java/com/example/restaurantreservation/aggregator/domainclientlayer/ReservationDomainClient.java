package com.example.restaurantreservation.aggregator.domainclientlayer;

import com.example.restaurantreservation.aggregator.domainclientlayer.dto.BookingClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreateBookingClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreatePreOrderClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.PreOrderClientResponse;

public interface ReservationDomainClient {
    BookingClientResponse getBookingById(Long bookingId);
    BookingClientResponse createBooking(CreateBookingClientRequest request);
    BookingClientResponse updateBooking(Long bookingId, CreateBookingClientRequest request);
    void deleteBooking(Long bookingId);
    PreOrderClientResponse createPreOrder(CreatePreOrderClientRequest request);
    PreOrderClientResponse updatePreOrder(Long preOrderId, CreatePreOrderClientRequest request);
    void deletePreOrder(Long preOrderId);
}
