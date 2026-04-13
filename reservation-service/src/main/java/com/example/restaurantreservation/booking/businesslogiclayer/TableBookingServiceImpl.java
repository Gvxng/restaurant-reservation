package com.example.restaurantreservation.booking.businesslogiclayer;

import com.example.restaurantreservation.booking.dataaccesslayer.PreOrderRepository;
import com.example.restaurantreservation.booking.dataaccesslayer.TableBookingRepository;
import com.example.restaurantreservation.booking.datamappinglayer.BookingMapper;
import com.example.restaurantreservation.booking.domain.OrderLineItem;
import com.example.restaurantreservation.booking.domain.PreOrder;
import com.example.restaurantreservation.booking.domain.TableBooking;
import com.example.restaurantreservation.booking.domain.enums.BookingStatus;
import com.example.restaurantreservation.booking.domain.enums.PreOrderStatus;
import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreateBookingRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.PreOrderSummaryDTO;
import com.example.restaurantreservation.exception.BusinessRuleViolationException;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.exception.TableAlreadyBookedException;
import com.example.restaurantreservation.floor.businesslogiclayer.DiningTableServiceImpl;
import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableSummaryDTO;
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
    private final BookingMapper bookingMapper;
    private final DiningTableServiceImpl diningTableService;

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
        return toEnrichedResponseDTO(getBookingOrThrow(id));
    }

    @Override
    public BookingResponseDTO create(CreateBookingRequestDTO dto) {
        validateBookingTimes(dto);
        diningTableService.assertReservable(dto.getTableId(), dto.getPartySize());
        assertNoOverlap(dto.getTableId(), dto.getReservationDate(), dto.getTimeSlotStart(), dto.getTimeSlotEnd());

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
        validateBookingTimes(dto);

        boolean tableChanged = !booking.getTableId().equals(dto.getTableId());
        boolean dateChanged = !booking.getReservationDate().equals(dto.getReservationDate());
        boolean startChanged = !booking.getTimeSlotStart().equals(dto.getTimeSlotStart());
        boolean endChanged = !booking.getTimeSlotEnd().equals(dto.getTimeSlotEnd());

        if (tableChanged || dateChanged || startChanged || endChanged) {
            diningTableService.assertReservable(dto.getTableId(), dto.getPartySize());
            assertNoOverlap(dto.getTableId(), dto.getReservationDate(), dto.getTimeSlotStart(), dto.getTimeSlotEnd());
        }

        booking.setTableId(dto.getTableId());
        booking.setReservationDate(dto.getReservationDate());
        booking.setTimeSlotStart(dto.getTimeSlotStart());
        booking.setTimeSlotEnd(dto.getTimeSlotEnd());
        booking.setPartySize(dto.getPartySize());
        if (dto.getStatus() != null) {
            booking.setStatus(dto.getStatus());
        }

        if (dto.getStatus() == BookingStatus.COMPLETED && booking.getLoyaltyPointsEarned() == 0) {
            booking.setLoyaltyPointsEarned(computeLoyaltyPoints(booking));
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

    @Override
    public PreOrderSummaryDTO createPreOrder(CreatePreOrderRequestDTO dto) {
        TableBooking booking = getBookingOrThrow(dto.getBookingId());
        assertBookingCanAcceptPreOrder(booking);

        PreOrder preOrder = PreOrder.builder()
                .bookingId(dto.getBookingId())
                .status(PreOrderStatus.DRAFT)
                .currency("CAD")
                .totalAmount(BigDecimal.ZERO)
                .build();

        preOrder.setTotalAmount(buildLineItems(preOrder, dto.getItems()));
        PreOrder saved = preOrderRepository.save(preOrder);

        booking.setPreOrderId(saved.getPreOrderId());
        bookingRepository.save(booking);

        return bookingMapper.toBasePreOrderSummaryDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PreOrderSummaryDTO getPreOrder(Long preOrderId) {
        PreOrder preOrder = preOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("PreOrder", preOrderId));
        return bookingMapper.toBasePreOrderSummaryDTO(preOrder);
    }

    @Override
    public PreOrderSummaryDTO updatePreOrder(Long preOrderId, CreatePreOrderRequestDTO dto) {
        PreOrder preOrder = preOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("PreOrder", preOrderId));

        TableBooking booking = getBookingOrThrow(preOrder.getBookingId());
        assertBookingCanAcceptPreOrder(booking);

        preOrder.getItems().clear();
        preOrder.setTotalAmount(buildLineItems(preOrder, dto.getItems()));

        return bookingMapper.toBasePreOrderSummaryDTO(preOrderRepository.save(preOrder));
    }

    @Override
    public void deletePreOrder(Long preOrderId) {
        if (!preOrderRepository.existsById(preOrderId)) {
            throw new ResourceNotFoundException("PreOrder", preOrderId);
        }
        preOrderRepository.deleteById(preOrderId);
    }

    private BookingResponseDTO toEnrichedResponseDTO(TableBooking booking) {
        BookingResponseDTO dto = bookingMapper.toBaseResponseDTO(booking);
        enrichWithTableData(dto, booking.getTableId());
        enrichWithPreOrderData(dto, booking.getPreOrderId());
        return dto;
    }

    private void enrichWithTableData(BookingResponseDTO dto, Long tableId) {
        if (tableId == null) {
            return;
        }
        try {
            DiningTableSummaryDTO tableSummary = diningTableService.getSummary(tableId);
            dto.setTable(tableSummary);
        } catch (Exception ignored) {
            // Return the booking even if the linked table row is missing.
        }
    }

    private void enrichWithPreOrderData(BookingResponseDTO dto, Long preOrderId) {
        if (preOrderId == null) {
            return;
        }
        Optional<PreOrder> preOrder = preOrderRepository.findById(preOrderId);
        preOrder.ifPresent(value -> dto.setPreOrder(bookingMapper.toBasePreOrderSummaryDTO(value)));
    }

    private void validateBookingTimes(CreateBookingRequestDTO dto) {
        if (!dto.getTimeSlotEnd().isAfter(dto.getTimeSlotStart())) {
            throw new BusinessRuleViolationException("Time slot end must be after start.");
        }
    }

    private void assertNoOverlap(Long tableId, java.time.LocalDate date, java.time.LocalTime start, java.time.LocalTime end) {
        boolean overlap = bookingRepository.existsOverlappingBooking(tableId, date, start, end);
        if (overlap) {
            throw new TableAlreadyBookedException("Table is already booked for the requested date and time slot.");
        }
    }

    private void assertBookingCanAcceptPreOrder(TableBooking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Cannot modify a PreOrder for a " + booking.getStatus() + " booking.");
        }
    }

    private TableBooking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TableBooking", id));
    }

    private int computeLoyaltyPoints(TableBooking booking) {
        int base = 100;
        if (booking.getPreOrderId() == null) {
            return base;
        }
        return preOrderRepository.findById(booking.getPreOrderId())
                .map(preOrder -> base + preOrder.getTotalAmount().intValue())
                .orElse(base);
    }

    private BigDecimal buildLineItems(
            PreOrder preOrder,
            List<CreatePreOrderRequestDTO.LineItemRequest> requests) {

        BigDecimal total = BigDecimal.ZERO;
        if (requests == null) {
            return total;
        }

        for (CreatePreOrderRequestDTO.LineItemRequest req : requests) {
            if (req.getQuantity() < 1) {
                throw new BusinessRuleViolationException("Line item quantity must be at least 1.");
            }
            if (req.getUnitAmount() == null || req.getUnitAmount().signum() <= 0) {
                throw new BusinessRuleViolationException("Line item unit amount must be greater than 0.");
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
