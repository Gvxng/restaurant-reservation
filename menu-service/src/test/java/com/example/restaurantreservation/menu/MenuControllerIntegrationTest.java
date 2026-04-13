package com.example.restaurantreservation.menu;

import com.example.restaurantreservation.menu.domain.enums.MenuCategory;
import com.example.restaurantreservation.menu.presentationlayer.dto.CreateMenuItemRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MenuControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUpClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Test
    void getAllMenuItemsReturnsSeededItems() {
        webTestClient.get()
                .uri("/api/v1/menu-items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> body.contains("Caesar Salad"));
    }

    @Test
    void getMenuItemByIdReturnsSeededItem() {
        webTestClient.get()
                .uri("/api/v1/menu-items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Caesar Salad");
    }

    @Test
    void createUpdateAndDeleteMenuItemWorks() {
        CreateMenuItemRequestDTO request = new CreateMenuItemRequestDTO();
        request.setMenuId(1L);
        request.setName("Citrus Tart");
        request.setDescription("Seasonal dessert");
        request.setAmount(new BigDecimal("9.75"));
        request.setCurrency("CAD");
        request.setCategory(MenuCategory.DESSERT);
        request.setAvailable(true);
        request.setDietaryTags("VEGETARIAN");

        String location = webTestClient.post()
                .uri("/api/v1/menu-items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.name").isEqualTo("Citrus Tart")
                .returnResult()
                .getResponseHeaders()
                .getLocation()
                .toString();

        long menuItemId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        request.setAmount(new BigDecimal("10.25"));

        webTestClient.put()
                .uri("/api/v1/menu-items/{id}", menuItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.amount").isEqualTo(10.25);

        webTestClient.delete()
                .uri("/api/v1/menu-items/{id}", menuItemId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/v1/menu-items/{id}", menuItemId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createMenuItemWithInvalidPriceReturnsConflict() {
        CreateMenuItemRequestDTO request = new CreateMenuItemRequestDTO();
        request.setMenuId(1L);
        request.setName("Broken Item");
        request.setDescription("Should fail");
        request.setAmount(BigDecimal.ZERO);
        request.setCurrency("CAD");
        request.setCategory(MenuCategory.DESSERT);
        request.setAvailable(true);

        webTestClient.post()
                .uri("/api/v1/menu-items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").value(message -> ((String) message).contains("price must be greater than 0"));
    }
}
