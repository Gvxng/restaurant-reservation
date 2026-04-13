package com.example.restaurantreservation.reservation;

import com.example.restaurantreservation.booking.dataaccesslayer.TableBookingRepository;
import com.example.restaurantreservation.floor.dataaccesslayer.DiningTableRepository;
import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("testing")
class ReservationRepositoryIntegrationTest {

    @Autowired
    private TableBookingRepository tableBookingRepository;

    @Autowired
    private DiningTableRepository diningTableRepository;

    @Test
    void existsOverlappingBookingReturnsTrueForMatchingTimeWindow() {
        boolean overlaps = tableBookingRepository.existsOverlappingBooking(
                2L,
                LocalDate.of(2030, 5, 20),
                LocalTime.of(19, 0),
                LocalTime.of(21, 0)
        );

        assertThat(overlaps).isTrue();
    }

    @Test
    void existsOverlappingBookingReturnsFalseForDifferentDate() {
        boolean overlaps = tableBookingRepository.existsOverlappingBooking(
                2L,
                LocalDate.of(2030, 5, 25),
                LocalTime.of(19, 0),
                LocalTime.of(21, 0)
        );

        assertThat(overlaps).isFalse();
    }

    @Test
    void existsByTableNumberReturnsTrueForSeededTable() {
        assertThat(diningTableRepository.existsByTableNumber("T02")).isTrue();
    }

    @Test
    void findByStatusReturnsOnlyMatchingTables() {
        assertThat(diningTableRepository.findByStatus(TableStatus.MAINTENANCE))
                .extracting("tableNumber")
                .containsExactly("T04");
    }
}
