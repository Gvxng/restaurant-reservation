package com.example.restaurantreservation.aggregator;

import com.example.restaurantreservation.aggregator.dataaccesslayer.ReservationAggregateRepository;
import com.example.restaurantreservation.aggregator.domain.ReservationAggregate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("testing")
class ReservationAggregateRepositoryIntegrationTest {

    @Autowired
    private ReservationAggregateRepository reservationAggregateRepository;

    @BeforeEach
    void setUp() {
        reservationAggregateRepository.deleteAll();
    }

    @Test
    void saveAndFindReservationAggregate() {
        ReservationAggregate saved = reservationAggregateRepository.save(aggregate("agg-1"));

        assertThat(reservationAggregateRepository.findById(saved.getAggregateId()))
                .isPresent()
                .get()
                .extracting("totalAmount")
                .isEqualTo(new BigDecimal("41.00"));
    }

    @Test
    void updateReservationAggregate() {
        ReservationAggregate aggregate = reservationAggregateRepository.save(aggregate("agg-1"));
        aggregate.setStatus("COMPLETED");
        aggregate.setTotalAmount(new BigDecimal("50.00"));
        reservationAggregateRepository.save(aggregate);

        assertThat(reservationAggregateRepository.findById("agg-1"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getStatus()).isEqualTo("COMPLETED");
                    assertThat(found.getTotalAmount()).isEqualByComparingTo("50.00");
                });
    }

    @Test
    void deleteReservationAggregate() {
        reservationAggregateRepository.save(aggregate("agg-1"));

        reservationAggregateRepository.deleteById("agg-1");

        assertThat(reservationAggregateRepository.findById("agg-1")).isEmpty();
    }

    @Test
    void findUnknownReservationAggregateReturnsEmpty() {
        assertThat(reservationAggregateRepository.findById("missing")).isEmpty();
    }

    private ReservationAggregate aggregate(String id) {
        return ReservationAggregate.builder()
                .aggregateId(id)
                .bookingId(10L)
                .preOrderId(20L)
                .customerId(101L)
                .tableId(2L)
                .reservationDate(LocalDate.now().plusDays(30))
                .timeSlotStart(LocalTime.of(18, 0))
                .timeSlotEnd(LocalTime.of(20, 0))
                .partySize(2)
                .status("PENDING")
                .totalAmount(new BigDecimal("41.00"))
                .currency("CAD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
