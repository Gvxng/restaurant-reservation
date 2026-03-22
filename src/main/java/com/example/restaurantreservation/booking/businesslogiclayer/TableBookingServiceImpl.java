package com.example.restaurantreservation.booking.businesslogiclayer;

import com.example.restaurantreservation.exception.BusinessRuleViolationException;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.floor.businesslogiclayer.DiningTableServiceImpl;
import com.example.restaurantreservation.loyalty.businesslogiclayer.LoyaltyAccountServiceImpl;
import com.example.restaurantreservation.booking.domain.OrderLineItem;
import com.example.restaurantreservation.booking.domain.PreOrder;
import com.example.restaurantreservation.booking.domain.TableBooking;
import com.example.restaurantreservation.booking.domain.enums.BookingStatus;
import com.example.restaurantreservation.booking.domain.enums.PreOrderStatus;
import com.example.restaurantreservation.booking.dataaccesslayer.PreOrderRepository;
import com.example.restaurantreservation.booking.dataaccesslayer.TableBookingRepository;
import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.booking.datamappinglayer.BookingMapper;
import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreateBookingRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.OrderLineItemDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.PreOrderSummaryDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableSummaryDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountSummaryDTO;
import com.example.restaurantreservation.menu.businesslogiclayer.MenuItemServiceImpl;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = Exception.class)
public class TableBookingServiceImpl implements TableBookingService {

    private final TableBookingRepository bookingRepository;
    private final PreOrderRepository preOrderRepository;
    private final BookingMapper bookingMapper;          // Data Mapping Layer

    // Cross-context service references (CA04 — same JVM; microservices would use HTTP clients)
    private final DiningTableServiceImpl diningTableService;
    private final LoyaltyAccountServiceImpl loyaltyAccountService;
    private final MenuItemServiceImpl menuItemService;

    // ================================================================
    // TableBooking CRUD
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> findAll() {
        List<TableBooking> bookings = bookingRepository.findAll();
        List<BookingResponseDTO> result = new ArrayList<>();
        for (TableBooking booking : bookings) {
            result.add(toEnrichedResponseDTO(booking));
        }
        return result;
    }
    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO findById(Long id) {
        TableBooking booking = getBookingOrThrow(id);
        return toEnrichedResponseDTO(booking);
    }
    @Override
    public BookingResponseDTO create(CreateBookingRequestDTO dto) {
        if (!dto.getTimeSlotEnd().isAfter(dto.getTimeSlotStart())) {
            throw new BusinessRuleViolationException("Time slot end must be after start.");
        }

        // INV-2 + INV-3 + INV-10: validate via Floor Layout
        diningTableService.assertReservable(dto.getTableId(), dto.getPartySize());

        // INV-1: double-booking guard
        boolean overlap = bookingRepository.existsOverlappingBooking(
                dto.getTableId(), dto.getReservationDate(),
                dto.getTimeSlotStart(), dto.getTimeSlotEnd());
        if (overlap) {
            throw new BusinessRuleViolationException(
                    "Table is already booked for the requested date and time slot.");
        }

        TableBooking booking = TableBooking.builder()
                .customerId(dto.getCustomerId())
                .tableId(dto.getTableId())
                .reservationDate(dto.getReservationDate())
                .timeSlotStart(dto.getTimeSlotStart())
                .timeSlotEnd(dto.getTimeSlotEnd())
                .partySize(dto.getPartySize())
                .status(dto.getStatus() != null ? dto.getStatus() : BookingStatus.PENDING)
                .loyaltyPointsEarned(0)
                .createdAt(LocalDateTime.now())
                .build();

        diningTableService.setStatus(dto.getTableId(), TableStatus.RESERVED);
        return toEnrichedResponseDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDTO update(Long id, CreateBookingRequestDTO dto) {
        TableBooking booking = getBookingOrThrow(id);

        if (!dto.getTimeSlotEnd().isAfter(dto.getTimeSlotStart())) {
            throw new BusinessRuleViolationException("Time slot end must be after start.");
        }

        boolean tableChanged = !booking.getTableId().equals(dto.getTableId());
        boolean dateChanged  = !booking.getReservationDate().equals(dto.getReservationDate());
        boolean timeChanged  = !booking.getTimeSlotStart().equals(dto.getTimeSlotStart());

        if (tableChanged || dateChanged || timeChanged) {
            diningTableService.assertReservable(dto.getTableId(), dto.getPartySize());
            boolean overlap = bookingRepository.existsOverlappingBooking(
                    dto.getTableId(), dto.getReservationDate(),
                    dto.getTimeSlotStart(), dto.getTimeSlotEnd());
            if (overlap) {
                throw new BusinessRuleViolationException(
                        "Table is already booked for the requested date and time slot.");
            }
        }

        booking.setTableId(dto.getTableId());
        booking.setReservationDate(dto.getReservationDate());
        booking.setTimeSlotStart(dto.getTimeSlotStart());
        booking.setTimeSlotEnd(dto.getTimeSlotEnd());
        booking.setPartySize(dto.getPartySize());
        if (dto.getStatus() != null) booking.setStatus(dto.getStatus());

        // Handle COMPLETED → earn loyalty points
        if (dto.getStatus() == BookingStatus.COMPLETED && booking.getLoyaltyPointsEarned() == 0) {
            int pointsToEarn = computeLoyaltyPoints(booking);
            booking.setLoyaltyPointsEarned(pointsToEarn);
            loyaltyAccountService.earnPoints(
                    booking.getCustomerId(), booking.getBookingId(), pointsToEarn);
            diningTableService.setStatus(booking.getTableId(), TableStatus.AVAILABLE);
        }

        if (dto.getStatus() == BookingStatus.CANCELLED) {
            diningTableService.setStatus(booking.getTableId(), TableStatus.AVAILABLE);
        }

        return toEnrichedResponseDTO(bookingRepository.save(booking));
    }
    @Override
    public void delete(Long id) {
        TableBooking booking = getBookingOrThrow(id);
        diningTableService.setStatus(booking.getTableId(), TableStatus.AVAILABLE);
        bookingRepository.deleteById(id);
    }

    // ================================================================
    // PreOrder CRUD
    // ================================================================
    @Override
    public PreOrderSummaryDTO createPreOrder(CreatePreOrderRequestDTO dto) {
        TableBooking booking = getBookingOrThrow(dto.getBookingId());

        // INV-4
        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Cannot create a PreOrder for a " + booking.getStatus() + " booking.");
        }

        PreOrder preOrder = PreOrder.builder()
                .bookingId(dto.getBookingId())
                .status(PreOrderStatus.DRAFT)
                .currency("CAD")
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = buildLineItems(preOrder, dto.getItems());
        preOrder.setTotalAmount(total);
        PreOrder saved = preOrderRepository.save(preOrder);

        booking.setPreOrderId(saved.getPreOrderId());
        bookingRepository.save(booking);

        return toEnrichedPreOrderSummary(saved);
    }
    @Override
    public PreOrderSummaryDTO getPreOrder(Long preOrderId) {
        PreOrder preOrder = preOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("PreOrder", preOrderId));
        return toEnrichedPreOrderSummary(preOrder);
    }
    @Override
    public PreOrderSummaryDTO updatePreOrder(Long preOrderId, CreatePreOrderRequestDTO dto) {
        PreOrder preOrder = preOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("PreOrder", preOrderId));

        TableBooking booking = getBookingOrThrow(preOrder.getBookingId());
        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Cannot modify a PreOrder for a " + booking.getStatus() + " booking.");
        }

        preOrder.getItems().clear();
        BigDecimal total = buildLineItems(preOrder, dto.getItems());
        preOrder.setTotalAmount(total);

        return toEnrichedPreOrderSummary(preOrderRepository.save(preOrder));
    }
    @Override
    public void deletePreOrder(Long preOrderId) {
        if (!preOrderRepository.existsById(preOrderId)) {
            throw new ResourceNotFoundException("PreOrder", preOrderId);
        }
        preOrderRepository.deleteById(preOrderId);
    }

    // ================================================================
    // R10 — Cross-context enrichment (CA04 pattern)
    // ================================================================

    /**
     * Builds the full aggregated BookingResponseDTO.
     * Step 1: Map entity to base DTO using BookingMapper (Data Mapping Layer).
     * Steps 2-4: Enrich with data from each supporting subdomain.
     */
    private BookingResponseDTO toEnrichedResponseDTO(TableBooking b) {
        // Step 1: Base mapping (Data Mapping Layer)
        BookingResponseDTO dto = bookingMapper.toBaseResponseDTO(b);

        // Step 2: Enrich with Floor Layout subdomain data
        enrichWithTableData(dto, b.getTableId());

        // Step 3: Enrich with Customer Loyalty subdomain data
        enrichWithLoyaltyData(dto, b.getCustomerId());

        // Step 4: Enrich with PreOrder + Menu Management subdomain data
        enrichWithPreOrderData(dto, b.getPreOrderId());

        return dto;
    }

    /**
     * CA04 — Enrichment Step 2: Floor Layout subdomain.
     * Fetches table details (number, capacity, type, status, section).
     * Uses .ifPresent equivalent — does not fail if table is missing.
     */
    private void enrichWithTableData(BookingResponseDTO dto, Long tableId) {
        if (tableId == null) return;
        try {
            DiningTableSummaryDTO tableSummary = diningTableService.getSummary(tableId);
            dto.setTable(tableSummary);
        } catch (Exception ignored) {
            // Graceful degradation — table data unavailable but booking is still returned
        }
    }

    /**
     * CA04 — Enrichment Step 3: Customer Loyalty subdomain.
     * Fetches loyalty account (points balance, tier) for the customer.
     * Returns null in the DTO field if the customer has no loyalty account.
     */
    private void enrichWithLoyaltyData(BookingResponseDTO dto, Long customerId) {
        if (customerId == null) return;
        LoyaltyAccountSummaryDTO loyaltySummary =
                loyaltyAccountService.getSummaryByCustomerId(customerId);
        dto.setLoyaltyAccount(loyaltySummary);
    }

    /**
     * CA04 — Enrichment Step 4: PreOrder aggregate + Menu Management subdomain.
     * Fetches the pre-order and enriches each line item with the menu item name
     * from the Menu Management bounded context.
     */
    private void enrichWithPreOrderData(BookingResponseDTO dto, Long preOrderId) {
        if (preOrderId == null) return;
        Optional<PreOrder> preOrderOpt = preOrderRepository.findById(preOrderId);
        if (preOrderOpt.isEmpty()) return;

        PreOrderSummaryDTO preOrderSummary = toEnrichedPreOrderSummary(preOrderOpt.get());
        dto.setPreOrder(preOrderSummary);
    }

    /**
     * Builds a PreOrderSummaryDTO and enriches each line item
     * with the menu item name from the Menu Management context.
     */
    private PreOrderSummaryDTO toEnrichedPreOrderSummary(PreOrder po) {
        // Step 1: base mapping
        PreOrderSummaryDTO dto = bookingMapper.toBasePreOrderSummaryDTO(po);

        // Step 2: enrich each line item with menu item name (Menu Management context)
        if (dto.getItems() != null) {
            for (OrderLineItemDTO item : dto.getItems()) {
                enrichLineItemWithMenuData(item);
            }
        }
        return dto;
    }

    /**
     * CA04 — Enriches a single OrderLineItemDTO with the menu item name
     * from the Menu Management bounded context.
     */
    private void enrichLineItemWithMenuData(OrderLineItemDTO item) {
        if (item.getMenuItemId() == null) return;
        try {
            MenuItemSummaryDTO menuItem = menuItemService.getSummary(item.getMenuItemId());
            item.setMenuItemName(menuItem.getName());
        } catch (Exception ignored) {
            // Graceful degradation — menu item name unavailable
        }
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private TableBooking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TableBooking", id));
    }

    private int computeLoyaltyPoints(TableBooking booking) {
        int base = 100;
        if (booking.getPreOrderId() == null) return base;
        Optional<PreOrder> po = preOrderRepository.findById(booking.getPreOrderId());
        return po.map(preOrder -> base + preOrder.getTotalAmount().intValue()).orElse(base);
    }

    /**
     * Shared line-item builder used by createPreOrder and updatePreOrder.
     * Validates each item against Menu Management context (INV menu availability).
     * Accumulates total for INV-5.
     */
    private BigDecimal buildLineItems(
            PreOrder preOrder,
            List<CreatePreOrderRequestDTO.LineItemRequest> requests) {

        BigDecimal total = BigDecimal.ZERO;
        if (requests == null) return total;

        for (CreatePreOrderRequestDTO.LineItemRequest req : requests) {
            MenuItemSummaryDTO menuItem = menuItemService.getSummary(req.getMenuItemId());
            if (!menuItem.isAvailable()) {
                throw new BusinessRuleViolationException(
                        "MenuItem '" + menuItem.getName() + "' is currently unavailable.");
            }
            OrderLineItem lineItem = OrderLineItem.builder()
                    .preOrder(preOrder)
                    .menuItemId(req.getMenuItemId())
                    .quantity(req.getQuantity())
                    .unitAmount(req.getUnitAmount())
                    .currency(req.getCurrency() != null ? req.getCurrency() : "CAD")
                    .build();
            preOrder.getItems().add(lineItem);
            total = total.add(req.getUnitAmount().multiply(BigDecimal.valueOf(req.getQuantity())));
        }
        return total;
    }
}
