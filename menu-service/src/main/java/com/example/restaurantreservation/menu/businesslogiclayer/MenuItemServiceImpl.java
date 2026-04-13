package com.example.restaurantreservation.menu.businesslogiclayer;

import com.example.restaurantreservation.exception.InvalidMenuItemPriceException;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.menu.dataaccesslayer.MenuItemRepository;
import com.example.restaurantreservation.menu.dataaccesslayer.MenuRepository;
import com.example.restaurantreservation.menu.datamappinglayer.MenuItemMapper;
import com.example.restaurantreservation.menu.domain.Menu;
import com.example.restaurantreservation.menu.domain.MenuItem;
import com.example.restaurantreservation.menu.presentationlayer.dto.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemResponseDTO;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuRepository menuRepository;
    private final MenuItemMapper menuItemMapper;

    @Override
    public List<MenuItemResponseDTO> findAll() {
        List<MenuItem> items = menuItemRepository.findAll();
        List<MenuItemResponseDTO> result = new ArrayList<>();
        for (MenuItem item : items) {
            result.add(menuItemMapper.toResponseDTO(item));
        }
        return result;
    }

    @Override
    public MenuItemResponseDTO findById(Long id) {
        return menuItemMapper.toResponseDTO(getOrThrow(id));
    }

    @Override
    public MenuItemSummaryDTO getSummary(Long id) {
        return menuItemMapper.toSummaryDTO(getOrThrow(id));
    }

    @Override
    public MenuItemResponseDTO create(CreateMenuItemRequestDTO dto) {
        validatePrice(dto.getAmount());

        MenuItem item = new MenuItem();
        applyDTO(item, dto);

        if (dto.getMenuId() != null) {
            Menu menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", dto.getMenuId()));
            item.setMenu(menu);
        }

        return menuItemMapper.toResponseDTO(menuItemRepository.save(item));
    }

    @Override
    public MenuItemResponseDTO update(Long id, CreateMenuItemRequestDTO dto) {
        MenuItem item = getOrThrow(id);
        validatePrice(dto.getAmount());
        applyDTO(item, dto);
        return menuItemMapper.toResponseDTO(menuItemRepository.save(item));
    }

    @Override
    public void delete(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("MenuItem", id);
        }
        menuItemRepository.deleteById(id);
    }

    private MenuItem getOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    private void validatePrice(java.math.BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidMenuItemPriceException("Menu item price must be greater than 0.");
        }
    }

    private void applyDTO(MenuItem item, CreateMenuItemRequestDTO dto) {
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAmount(dto.getAmount());
        item.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "CAD");
        item.setCategory(dto.getCategory());
        item.setAvailable(dto.isAvailable());
        item.setDietaryTags(dto.getDietaryTags());
    }
}
