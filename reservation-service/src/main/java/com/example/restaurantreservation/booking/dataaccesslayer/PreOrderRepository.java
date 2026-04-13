package com.example.restaurantreservation.booking.dataaccesslayer;

import com.example.restaurantreservation.booking.domain.PreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreOrderRepository extends JpaRepository<PreOrder, Long> {
    Optional<PreOrder> findByBookingId(Long bookingId);
    List<PreOrder> findAllByBookingId(Long bookingId);
}
