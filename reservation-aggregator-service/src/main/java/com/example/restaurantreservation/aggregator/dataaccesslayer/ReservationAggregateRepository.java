package com.example.restaurantreservation.aggregator.dataaccesslayer;

import com.example.restaurantreservation.aggregator.domain.ReservationAggregate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationAggregateRepository extends MongoRepository<ReservationAggregate, String> {
}
