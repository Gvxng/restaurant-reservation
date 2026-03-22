package com.example.restaurantreservation.booking.domain;

import com.example.restaurantreservation.booking.domain.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(name = "table_bookings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    // Cross-context ID reference (Customer Loyalty context)
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Cross-context ID reference (Floor Layout context)
    @Column(name = "table_id", nullable = false)
    private Long tableId;

    // Cross-context ID reference (PreOrder aggregate)
    @Column(name = "pre_order_id")
    private Long preOrderId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    // Value Object: TimeSlot (embedded as two columns)
    @Column(name = "time_slot_start", nullable = false)
    private LocalTime timeSlotStart;

    @Column(name = "time_slot_end", nullable = false)
    private LocalTime timeSlotEnd;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "loyalty_points_earned", nullable = false)
    private int loyaltyPointsEarned = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
