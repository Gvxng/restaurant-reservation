package com.example.restaurantreservation.apigateway.businesslogic;

import com.example.restaurantreservation.apigateway.presentation.dto.booking.BookingResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreateBookingRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.PreOrderSummaryDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.DiningTableResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationGatewayServiceImpl implements ReservationGatewayService {

    private final RestTemplate restTemplate;

    @Value("${microservices.reservation.base-url}")
    private String reservationBaseUrl;

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        return restTemplate.exchange(
                reservationBaseUrl + "/api/v1/bookings",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookingResponseDTO>>() {
                }
        ).getBody();
    }

    @Override
    public BookingResponseDTO getBookingById(Long id) {
        return restTemplate.getForObject(reservationBaseUrl + "/api/v1/bookings/" + id, BookingResponseDTO.class);
    }

    @Override
    public BookingResponseDTO createBooking(CreateBookingRequestDTO request) {
        return restTemplate.postForObject(reservationBaseUrl + "/api/v1/bookings", request, BookingResponseDTO.class);
    }

    @Override
    public BookingResponseDTO updateBooking(Long id, CreateBookingRequestDTO request) {
        ResponseEntity<BookingResponseDTO> response = restTemplate.exchange(
                reservationBaseUrl + "/api/v1/bookings/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                BookingResponseDTO.class
        );
        return response.getBody();
    }

    @Override
    public void deleteBooking(Long id) {
        restTemplate.delete(reservationBaseUrl + "/api/v1/bookings/" + id);
    }

    @Override
    public PreOrderSummaryDTO getPreOrderById(Long id) {
        return restTemplate.getForObject(reservationBaseUrl + "/api/v1/pre-orders/" + id, PreOrderSummaryDTO.class);
    }

    @Override
    public PreOrderSummaryDTO createPreOrder(CreatePreOrderRequestDTO request) {
        return restTemplate.postForObject(reservationBaseUrl + "/api/v1/pre-orders", request, PreOrderSummaryDTO.class);
    }

    @Override
    public PreOrderSummaryDTO updatePreOrder(Long id, CreatePreOrderRequestDTO request) {
        return restTemplate.exchange(
                reservationBaseUrl + "/api/v1/pre-orders/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                PreOrderSummaryDTO.class
        ).getBody();
    }

    @Override
    public void deletePreOrder(Long id) {
        restTemplate.delete(reservationBaseUrl + "/api/v1/pre-orders/" + id);
    }

    @Override
    public List<DiningTableResponseDTO> getAllDiningTables() {
        return restTemplate.exchange(
                reservationBaseUrl + "/api/v1/dining-tables",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DiningTableResponseDTO>>() {
                }
        ).getBody();
    }

    @Override
    public DiningTableResponseDTO getDiningTableById(Long id) {
        return restTemplate.getForObject(
                reservationBaseUrl + "/api/v1/dining-tables/" + id,
                DiningTableResponseDTO.class
        );
    }

    @Override
    public DiningTableResponseDTO createDiningTable(CreateDiningTableRequestDTO request) {
        return restTemplate.postForObject(
                reservationBaseUrl + "/api/v1/dining-tables",
                request,
                DiningTableResponseDTO.class
        );
    }

    @Override
    public DiningTableResponseDTO updateDiningTable(Long id, CreateDiningTableRequestDTO request) {
        return restTemplate.exchange(
                reservationBaseUrl + "/api/v1/dining-tables/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                DiningTableResponseDTO.class
        ).getBody();
    }

    @Override
    public void deleteDiningTable(Long id) {
        restTemplate.delete(reservationBaseUrl + "/api/v1/dining-tables/" + id);
    }
}
