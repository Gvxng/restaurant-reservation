package com.example.restaurantreservation.aggregator.businesslogiclayer;

import com.example.restaurantreservation.aggregator.dataaccesslayer.ReservationAggregateRepository;
import com.example.restaurantreservation.aggregator.datamappinglayer.ReservationAggregateMapper;
import com.example.restaurantreservation.aggregator.domain.DiningTableSnapshot;
import com.example.restaurantreservation.aggregator.domain.LoyaltyAccountSnapshot;
import com.example.restaurantreservation.aggregator.domain.PreOrderItemSnapshot;
import com.example.restaurantreservation.aggregator.domain.ReservationAggregate;
import com.example.restaurantreservation.aggregator.domainclientlayer.LoyaltyDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.MenuDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.ReservationDomainClient;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.BookingClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreateBookingClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.CreatePreOrderClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.DiningTableClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.LoyaltyAccountClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.MenuItemClientResponse;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.PreOrderClientResponse;
import com.example.restaurantreservation.aggregator.exception.InvalidInputException;
import com.example.restaurantreservation.aggregator.exception.MenuItemUnavailableException;
import com.example.restaurantreservation.aggregator.exception.NotFoundException;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.CreateReservationRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.PreOrderItemRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.ReservationAggregateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationAggregatorServiceImpl implements ReservationAggregatorService {

    private static final String COMPLETED_STATUS = "COMPLETED";

    private final ReservationAggregateRepository reservationAggregateRepository;
    private final ReservationDomainClient reservationDomainClient;
    private final MenuDomainClient menuDomainClient;
    private final LoyaltyDomainClient loyaltyDomainClient;
    private final ReservationAggregateMapper reservationAggregateMapper;

    @Override
    public List<ReservationAggregateResponseDTO> findAll() {
        return reservationAggregateRepository.findAll().stream()
                .map(reservationAggregateMapper::toResponseDTO)
                .toList();
    }

    @Override
    public ReservationAggregateResponseDTO findById(String aggregateId) {
        return reservationAggregateMapper.toResponseDTO(getAggregateOrThrow(aggregateId));
    }

    @Override
    public ReservationAggregateResponseDTO create(CreateReservationRequestDTO request) {
        validateRequest(request);
        PreOrderPlan preOrderPlan = buildPreOrderPlan(request.getPreOrderItems());

        BookingClientResponse booking = reservationDomainClient.createBooking(toBookingRequest(request));
        PreOrderClientResponse preOrder = reservationDomainClient.createPreOrder(toPreOrderRequest(booking.getBookingId(), preOrderPlan));
        BookingClientResponse refreshedBooking = reservationDomainClient.getBookingById(booking.getBookingId());
        Optional<LoyaltyAccountClientResponse> loyalty = loyaltyDomainClient.getLoyaltyAccountByCustomerId(request.getCustomerId());

        ReservationAggregate aggregate = ReservationAggregate.builder()
                .aggregateId(UUID.randomUUID().toString())
                .bookingId(refreshedBooking.getBookingId())
                .preOrderId(preOrder.getPreOrderId())
                .customerId(request.getCustomerId())
                .tableId(request.getTableId())
                .reservationDate(request.getReservationDate())
                .timeSlotStart(request.getTimeSlotStart())
                .timeSlotEnd(request.getTimeSlotEnd())
                .partySize(request.getPartySize())
                .status(refreshedBooking.getStatus())
                .totalAmount(preOrderPlan.totalAmount())
                .currency(preOrderPlan.currency())
                .loyaltyPointsEarned(refreshedBooking.getLoyaltyPointsEarned())
                .table(toTableSnapshot(refreshedBooking.getTable()))
                .loyaltyAccount(loyalty.map(this::toLoyaltySnapshot).orElse(null))
                .preOrderItems(preOrderPlan.itemSnapshots())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return reservationAggregateMapper.toResponseDTO(reservationAggregateRepository.save(aggregate));
    }

    @Override
    public ReservationAggregateResponseDTO update(String aggregateId, CreateReservationRequestDTO request) {
        validateRequest(request);
        ReservationAggregate aggregate = getAggregateOrThrow(aggregateId);
        PreOrderPlan preOrderPlan = buildPreOrderPlan(request.getPreOrderItems());

        BookingClientResponse booking = reservationDomainClient.updateBooking(aggregate.getBookingId(), toBookingRequest(request));
        PreOrderClientResponse preOrder;
        if (aggregate.getPreOrderId() == null) {
            preOrder = reservationDomainClient.createPreOrder(toPreOrderRequest(booking.getBookingId(), preOrderPlan));
        } else {
            preOrder = reservationDomainClient.updatePreOrder(aggregate.getPreOrderId(), toPreOrderRequest(booking.getBookingId(), preOrderPlan));
        }

        Optional<LoyaltyAccountClientResponse> loyalty = loyaltyDomainClient.getLoyaltyAccountByCustomerId(request.getCustomerId());
        boolean loyaltyAlreadyAwarded = aggregate.getLoyaltyPointsEarned() != null && aggregate.getLoyaltyPointsEarned() > 0;
        if (COMPLETED_STATUS.equalsIgnoreCase(booking.getStatus()) && booking.getLoyaltyPointsEarned() > 0 && !loyaltyAlreadyAwarded) {
            Optional<LoyaltyAccountClientResponse> existingLoyalty = loyalty;
            loyalty = loyaltyDomainClient.earnPoints(request.getCustomerId(), booking.getBookingId(), booking.getLoyaltyPointsEarned())
                    .or(() -> existingLoyalty);
        }

        aggregate.setBookingId(booking.getBookingId());
        aggregate.setPreOrderId(preOrder.getPreOrderId());
        aggregate.setCustomerId(request.getCustomerId());
        aggregate.setTableId(request.getTableId());
        aggregate.setReservationDate(request.getReservationDate());
        aggregate.setTimeSlotStart(request.getTimeSlotStart());
        aggregate.setTimeSlotEnd(request.getTimeSlotEnd());
        aggregate.setPartySize(request.getPartySize());
        aggregate.setStatus(booking.getStatus());
        aggregate.setTotalAmount(preOrderPlan.totalAmount());
        aggregate.setCurrency(preOrderPlan.currency());
        aggregate.setLoyaltyPointsEarned(booking.getLoyaltyPointsEarned());
        aggregate.setTable(toTableSnapshot(booking.getTable()));
        aggregate.setLoyaltyAccount(loyalty.map(this::toLoyaltySnapshot).orElse(null));
        aggregate.setPreOrderItems(preOrderPlan.itemSnapshots());
        aggregate.setUpdatedAt(LocalDateTime.now());

        return reservationAggregateMapper.toResponseDTO(reservationAggregateRepository.save(aggregate));
    }

    @Override
    public void delete(String aggregateId) {
        ReservationAggregate aggregate = getAggregateOrThrow(aggregateId);
        if (aggregate.getPreOrderId() != null) {
            reservationDomainClient.deletePreOrder(aggregate.getPreOrderId());
        }
        reservationDomainClient.deleteBooking(aggregate.getBookingId());
        reservationAggregateRepository.deleteById(aggregateId);
    }

    private ReservationAggregate getAggregateOrThrow(String aggregateId) {
        return reservationAggregateRepository.findById(aggregateId)
                .orElseThrow(() -> new NotFoundException("ReservationAggregate", aggregateId));
    }

    private void validateRequest(CreateReservationRequestDTO request) {
        if (!request.getTimeSlotEnd().isAfter(request.getTimeSlotStart())) {
            throw new InvalidInputException("Time slot end must be after start.");
        }
        if (request.getPreOrderItems() == null || request.getPreOrderItems().isEmpty()) {
            throw new InvalidInputException("At least one pre-order item is required.");
        }
    }

    private PreOrderPlan buildPreOrderPlan(List<PreOrderItemRequestDTO> requestedItems) {
        List<PreOrderItemSnapshot> snapshots = new ArrayList<>();
        List<CreatePreOrderClientRequest.LineItemRequest> downstreamItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        String currency = "CAD";

        for (PreOrderItemRequestDTO requestedItem : requestedItems) {
            if (requestedItem.getQuantity() < 1) {
                throw new InvalidInputException("Line item quantity must be at least 1.");
            }

            MenuItemClientResponse menuItem = menuDomainClient.getMenuItemById(requestedItem.getMenuItemId());
            if (!menuItem.isAvailable()) {
                throw new MenuItemUnavailableException(requestedItem.getMenuItemId());
            }

            String itemCurrency = menuItem.getCurrency() != null ? menuItem.getCurrency() : "CAD";
            BigDecimal lineTotal = menuItem.getAmount().multiply(BigDecimal.valueOf(requestedItem.getQuantity()));
            total = total.add(lineTotal);
            currency = itemCurrency;

            snapshots.add(PreOrderItemSnapshot.builder()
                    .menuItemId(menuItem.getMenuItemId())
                    .name(menuItem.getName())
                    .quantity(requestedItem.getQuantity())
                    .unitAmount(menuItem.getAmount())
                    .lineTotal(lineTotal)
                    .currency(itemCurrency)
                    .category(menuItem.getCategory())
                    .build());

            downstreamItems.add(CreatePreOrderClientRequest.LineItemRequest.builder()
                    .menuItemId(menuItem.getMenuItemId())
                    .quantity(requestedItem.getQuantity())
                    .unitAmount(menuItem.getAmount())
                    .currency(itemCurrency)
                    .build());
        }

        return new PreOrderPlan(snapshots, downstreamItems, total, currency);
    }

    private CreateBookingClientRequest toBookingRequest(CreateReservationRequestDTO request) {
        return CreateBookingClientRequest.builder()
                .customerId(request.getCustomerId())
                .tableId(request.getTableId())
                .reservationDate(request.getReservationDate())
                .timeSlotStart(request.getTimeSlotStart())
                .timeSlotEnd(request.getTimeSlotEnd())
                .partySize(request.getPartySize())
                .status(request.getStatus())
                .build();
    }

    private CreatePreOrderClientRequest toPreOrderRequest(Long bookingId, PreOrderPlan preOrderPlan) {
        return CreatePreOrderClientRequest.builder()
                .bookingId(bookingId)
                .items(preOrderPlan.downstreamItems())
                .build();
    }

    private DiningTableSnapshot toTableSnapshot(DiningTableClientResponse table) {
        if (table == null) {
            return null;
        }
        return DiningTableSnapshot.builder()
                .tableId(table.getTableId())
                .tableNumber(table.getTableNumber())
                .seatingCapacity(table.getSeatingCapacity())
                .tableType(table.getTableType())
                .status(table.getStatus())
                .sectionName(table.getSectionName())
                .build();
    }

    private LoyaltyAccountSnapshot toLoyaltySnapshot(LoyaltyAccountClientResponse loyaltyAccount) {
        return LoyaltyAccountSnapshot.builder()
                .accountId(loyaltyAccount.getAccountId())
                .customerId(loyaltyAccount.getCustomerId())
                .pointsBalance(loyaltyAccount.getPointsBalance())
                .tier(loyaltyAccount.getTier())
                .build();
    }

    private record PreOrderPlan(
            List<PreOrderItemSnapshot> itemSnapshots,
            List<CreatePreOrderClientRequest.LineItemRequest> downstreamItems,
            BigDecimal totalAmount,
            String currency) {
    }
}
