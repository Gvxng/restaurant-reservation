package com.example.restaurantreservation.loyalty;

import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
class LoyaltyControllerIntegrationTest {

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
    void getAllLoyaltyAccountsReturnsSeededAccounts() {
        webTestClient.get()
                .uri("/api/v1/loyalty-accounts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3);
    }

    @Test
    void getLoyaltyAccountByIdReturnsSeededAccount() {
        webTestClient.get()
                .uri("/api/v1/loyalty-accounts/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customerId").isEqualTo(101)
                .jsonPath("$.tier").isEqualTo("SILVER");
    }

    @Test
    void createUpdateAndDeleteLoyaltyAccountWorks() {
        CreateLoyaltyAccountRequestDTO request = new CreateLoyaltyAccountRequestDTO();
        request.setCustomerId(999L);
        request.setPointsBalance(100);
        request.setTier(LoyaltyTier.BRONZE);
        request.setEnrollmentDate(LocalDate.now());

        LoyaltyAccountResponseDTO created = webTestClient.post()
                .uri("/api/v1/loyalty-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LoyaltyAccountResponseDTO.class)
                .returnResult()
                .getResponseBody();

        request.setPointsBalance(4000);
        request.setTier(LoyaltyTier.GOLD);

        webTestClient.put()
                .uri("/api/v1/loyalty-accounts/{id}", created.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pointsBalance").isEqualTo(4000)
                .jsonPath("$.tier").isEqualTo("GOLD");

        webTestClient.delete()
                .uri("/api/v1/loyalty-accounts/{id}", created.getAccountId())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/v1/loyalty-accounts/{id}", created.getAccountId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createLoyaltyAccountWithDuplicateCustomerReturnsConflict() {
        CreateLoyaltyAccountRequestDTO request = new CreateLoyaltyAccountRequestDTO();
        request.setCustomerId(101L);
        request.setPointsBalance(0);
        request.setTier(LoyaltyTier.BRONZE);
        request.setEnrollmentDate(LocalDate.now());

        webTestClient.post()
                .uri("/api/v1/loyalty-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").value(message -> ((String) message).contains("already has a loyalty account"));
    }

    @Test
    void updateLoyaltyAccountWithNegativePointsReturnsConflict() {
        CreateLoyaltyAccountRequestDTO request = new CreateLoyaltyAccountRequestDTO();
        request.setCustomerId(101L);
        request.setPointsBalance(-10);
        request.setTier(LoyaltyTier.SILVER);
        request.setEnrollmentDate(LocalDate.now());

        webTestClient.put()
                .uri("/api/v1/loyalty-accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").value(message -> ((String) message).contains("cannot be negative"));
    }
}
