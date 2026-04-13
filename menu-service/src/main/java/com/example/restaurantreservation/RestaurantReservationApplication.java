package com.example.restaurantreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.restaurantreservation.menu",
        "com.example.restaurantreservation.exception"
})
@EnableJpaRepositories(basePackages = "com.example.restaurantreservation.menu.dataaccesslayer")
public class RestaurantReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantReservationApplication.class, args);
    }

}
