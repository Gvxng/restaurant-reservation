package com.example.restaurantreservation.menu;

import com.example.restaurantreservation.menu.dataaccesslayer.MenuItemRepository;
import com.example.restaurantreservation.menu.domain.enums.MenuCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("testing")
class MenuRepositoryIntegrationTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    void findByIsAvailableTrueReturnsSeededItems() {
        assertThat(menuItemRepository.findByIsAvailableTrue()).hasSize(5);
    }

    @Test
    void findByCategoryReturnsMatchingItems() {
        assertThat(menuItemRepository.findByCategory(MenuCategory.MAIN))
                .extracting("name")
                .containsExactlyInAnyOrder("Grilled Salmon", "Mushroom Risotto");
    }

    @Test
    void findByMenuMenuIdReturnsItemsForKnownMenu() {
        assertThat(menuItemRepository.findByMenuMenuId(2L))
                .extracting("name")
                .containsExactly("Sparkling Water");
    }

    @Test
    void findByMenuMenuIdReturnsEmptyListForUnknownMenu() {
        assertThat(menuItemRepository.findByMenuMenuId(999L)).isEmpty();
    }
}
