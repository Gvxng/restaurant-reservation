package com.example.restaurantreservation.aggregator.domainclientlayer;

import com.example.restaurantreservation.aggregator.exception.DownstreamServiceException;
import com.example.restaurantreservation.aggregator.exception.InvalidInputException;
import com.example.restaurantreservation.aggregator.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

abstract class DomainClientSupport {

    RuntimeException mapResponseException(String serviceName, WebClientResponseException ex) {
        if (ex.getStatusCode().value() == 404) {
            return new NotFoundException(serviceName + " resource not found: " + ex.getResponseBodyAsString());
        }
        if (ex.getStatusCode().value() == 400) {
            return new InvalidInputException(serviceName + " rejected the request: " + ex.getResponseBodyAsString());
        }
        return new DownstreamServiceException(
                serviceName + " returned HTTP " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(),
                ex.getStatusCode());
    }

    RuntimeException mapRequestException(String serviceName, WebClientRequestException ex) {
        return new DownstreamServiceException(serviceName + " is unavailable: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }
}
