#!/usr/bin/env bash

spring init \
--boot-version=4.0.3 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=reservation-service \
--artifactId=reservation-service \
--package-name=com.example.restaurantreservation \
--groupId=com.example.restaurantreservation \
--dependencies=web,data-jpa,validation \
--version=1.0.0-SNAPSHOT \
reservation-service

spring init \
--boot-version=4.0.3 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=menu-service \
--artifactId=menu-service \
--package-name=com.example.restaurantreservation \
--groupId=com.example.restaurantreservation \
--dependencies=web,data-jpa,validation \
--version=1.0.0-SNAPSHOT \
menu-service

spring init \
--boot-version=4.0.3 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=loyalty-service \
--artifactId=loyalty-service \
--package-name=com.example.restaurantreservation \
--groupId=com.example.restaurantreservation \
--dependencies=web,data-jpa,validation \
--version=1.0.0-SNAPSHOT \
loyalty-service

spring init \
--boot-version=4.0.3 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=api-gateway-service \
--artifactId=api-gateway-service \
--package-name=com.example.restaurantreservation \
--groupId=com.example.restaurantreservation \
--dependencies=web,validation,hateoas \
--version=1.0.0-SNAPSHOT \
api-gateway-service
