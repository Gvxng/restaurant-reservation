package com.example.restaurantreservation.aggregator;

import com.example.restaurantreservation.aggregator.businesslogiclayer.ReservationAggregatorService;
import com.example.restaurantreservation.aggregator.exception.NotFoundException;
import com.example.restaurantreservation.aggregator.presentationlayer.ReservationAggregatorController;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.CreateReservationRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.ReservationAggregateResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationAggregatorControllerUnitTest {

    @Mock
    private ReservationAggregatorService reservationAggregatorService;

    @InjectMocks
    private ReservationAggregatorController reservationAggregatorController;

    @Test
    void getAllReturnsOk() {
        when(reservationAggregatorService.findAll()).thenReturn(List.of(response("agg-1")));

        ResponseEntity<List<ReservationAggregateResponseDTO>> response = reservationAggregatorController.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void createReturnsCreatedWithLocation() {
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        when(reservationAggregatorService.create(request)).thenReturn(response("agg-1"));
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/v1/reservations");
        servletRequest.setServerName("localhost");
        servletRequest.setServerPort(80);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        ResponseEntity<ReservationAggregateResponseDTO> response = reservationAggregatorController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("http://localhost/api/v1/reservations/agg-1");
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getByIdReturnsOk() {
        when(reservationAggregatorService.findById("agg-1")).thenReturn(response("agg-1"));

        ResponseEntity<ReservationAggregateResponseDTO> response = reservationAggregatorController.getById("agg-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAggregateId()).isEqualTo("agg-1");
    }

    @Test
    void getByIdPropagatesNotFound() {
        when(reservationAggregatorService.findById("missing")).thenThrow(new NotFoundException("ReservationAggregate", "missing"));

        assertThatThrownBy(() -> reservationAggregatorController.getById("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateReturnsOk() {
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        when(reservationAggregatorService.update("agg-1", request)).thenReturn(response("agg-1"));

        ResponseEntity<ReservationAggregateResponseDTO> response = reservationAggregatorController.update("agg-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAggregateId()).isEqualTo("agg-1");
    }

    @Test
    void deleteReturnsNoContent() {
        ResponseEntity<Void> response = reservationAggregatorController.delete("agg-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reservationAggregatorService).delete("agg-1");
    }

    private ReservationAggregateResponseDTO response(String id) {
        ReservationAggregateResponseDTO response = new ReservationAggregateResponseDTO();
        response.setAggregateId(id);
        response.setBookingId(10L);
        response.setTableId(2L);
        return response;
    }
}
