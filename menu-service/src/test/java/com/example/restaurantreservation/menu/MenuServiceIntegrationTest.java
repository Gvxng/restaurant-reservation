package com.example.restaurantreservation.menu;

import com.example.restaurantreservation.menu.businesslogiclayer.MenuItemService;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemSummaryDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("testing")
class MenuServiceIntegrationTest {

    @Autowired
    private MenuItemService menuItemService;

    @Test
    void getSummaryReturnsMappedMenuItemProjection() {
        MenuItemSummaryDTO summary = menuItemService.getSummary(1L);

        assertThat(summary.getMenuItemId()).isEqualTo(1L);
        assertThat(summary.getName()).isEqualTo("Caesar Salad");
        assertThat(summary.getAmount()).hasToString("14.50");
        assertThat(summary.getCurrency()).isEqualTo("CAD");
        assertThat(summary.isAvailable()).isTrue();
    }
}
