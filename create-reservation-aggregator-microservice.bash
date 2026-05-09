#!/usr/bin/env bash
spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=reservation-aggregator-service \
--artifact-id=reservation-aggregator-service \
--package-name=com.example.restaurantreservation \
--group-id=com.example.restaurantreservation \
--dependencies=web,webflux,data-mongodb,validation,actuator,lombok \
--version=1.0.0-SNAPSHOT \
reservation-aggregator-service
