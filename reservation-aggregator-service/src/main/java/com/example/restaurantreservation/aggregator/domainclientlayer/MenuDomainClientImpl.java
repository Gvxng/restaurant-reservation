package com.example.restaurantreservation.aggregator.domainclientlayer;

import com.example.restaurantreservation.aggregator.domainclientlayer.dto.MenuItemClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class MenuDomainClientImpl extends DomainClientSupport implements MenuDomainClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.menu.base-url}")
    private String menuBaseUrl;

    @Override
    public MenuItemClientResponse getMenuItemById(Long menuItemId) {
        try {
            return webClient().get()
                    .uri("/api/v1/menu-items/{id}", menuItemId)
                    .retrieve()
                    .bodyToMono(MenuItemClientResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw mapResponseException("menu-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("menu-service", ex);
        }
    }

    private WebClient webClient() {
        return webClientBuilder.baseUrl(menuBaseUrl).build();
    }
}
