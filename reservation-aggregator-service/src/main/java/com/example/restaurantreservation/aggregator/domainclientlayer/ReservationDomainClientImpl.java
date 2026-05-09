package com.example.restaurantreservation.aggregator.domainclientlayer;

import com.example.restaurantreservation.aggregator.domainclientlayer.dto.BookingClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreateBookingClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreatePreOrderClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.PreOrderClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class ReservationDomainClientImpl extends DomainClientSupport implements ReservationDomainClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.reservation.base-url}")
    private String reservationBaseUrl;

    @Override
    public BookingClientResponse getBookingById(Long bookingId) {
        return get("/api/v1/bookings/" + bookingId, BookingClientResponse.class);
    }

    @Override
    public BookingClientResponse createBooking(CreateBookingClientRequest request) {
        return post("/api/v1/bookings", request, BookingClientResponse.class);
    }

    @Override
    public BookingClientResponse updateBooking(Long bookingId, CreateBookingClientRequest request) {
        try {
            return webClient().put()
                    .uri("/api/v1/bookings/{id}", bookingId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(BookingClientResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw mapResponseException("reservation-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("reservation-service", ex);
        }
    }

    @Override
    public void deleteBooking(Long bookingId) {
        delete("/api/v1/bookings/" + bookingId);
    }

    @Override
    public PreOrderClientResponse createPreOrder(CreatePreOrderClientRequest request) {
        return post("/api/v1/pre-orders", request, PreOrderClientResponse.class);
    }

    @Override
    public PreOrderClientResponse updatePreOrder(Long preOrderId, CreatePreOrderClientRequest request) {
        try {
            return webClient().put()
                    .uri("/api/v1/pre-orders/{id}", preOrderId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PreOrderClientResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw mapResponseException("reservation-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("reservation-service", ex);
        }
    }

    @Override
    public void deletePreOrder(Long preOrderId) {
        delete("/api/v1/pre-orders/" + preOrderId);
    }

    private <T> T get(String uri, Class<T> responseType) {
        try {
            return webClient().get().uri(uri).retrieve().bodyToMono(responseType).block();
        } catch (WebClientResponseException ex) {
            throw mapResponseException("reservation-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("reservation-service", ex);
        }
    }

    private <T> T post(String uri, Object request, Class<T> responseType) {
        try {
            return webClient().post().uri(uri).bodyValue(request).retrieve().bodyToMono(responseType).block();
        } catch (WebClientResponseException ex) {
            throw mapResponseException("reservation-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("reservation-service", ex);
        }
    }

    private void delete(String uri) {
        try {
            webClient().delete().uri(uri).retrieve().toBodilessEntity().block();
        } catch (WebClientResponseException ex) {
            throw mapResponseException("reservation-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("reservation-service", ex);
        }
    }

    private WebClient webClient() {
        return webClientBuilder.baseUrl(reservationBaseUrl).build();
    }
}
