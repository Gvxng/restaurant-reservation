package com.example.restaurantreservation.booking.dataaccesslayer;

import com.example.restaurantreservation.booking.domain.TableBooking;
import com.example.restaurantreservation.booking.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, Long> {

    List<TableBooking> findByCustomerId(Long customerId);
    List<TableBooking> findByStatus(BookingStatus status);

    /**
     * Invariant check: double-booking guard.
     * A table is unavailable if there is already an active booking
     * on the same date with an overlapping time slot.
     */
    @Query("""
        SELECT COUNT(b) > 0 FROM TableBooking b
        WHERE b.tableId = :tableId
          AND b.reservationDate = :date
          AND b.status NOT IN ('CANCELLED')
          AND b.timeSlotStart < :end
          AND b.timeSlotEnd   > :start
    """)
    boolean existsOverlappingBooking(
        @Param("tableId") Long tableId,
        @Param("date")    LocalDate date,
        @Param("start")   LocalTime start,
        @Param("end")     LocalTime end
    );
}
