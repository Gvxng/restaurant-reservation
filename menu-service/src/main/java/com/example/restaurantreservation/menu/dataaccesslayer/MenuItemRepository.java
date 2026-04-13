package com.example.restaurantreservation.menu.dataaccesslayer;

import com.example.restaurantreservation.menu.domain.MenuItem;
import com.example.restaurantreservation.menu.domain.enums.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByIsAvailableTrue();
    List<MenuItem> findByCategory(MenuCategory category);
    List<MenuItem> findByMenuMenuId(Long menuId);
}
