package com.mustafa_mert.backend.ticket_purchase.service;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.common.exception.ErrorMessage;
import com.mustafa_mert.backend.common.exception.MessageType;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.mapper.EventMapper;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.security.CurrentUserProvider;
import com.mustafa_mert.backend.ticket_purchase.dto.PurchaseTicketRequest;
import com.mustafa_mert.backend.ticket_purchase.dto.TicketPurchaseResponse;
import com.mustafa_mert.backend.ticket_purchase.entity.TicketPurchase;
import com.mustafa_mert.backend.ticket_purchase.repository.TicketPurchaseRepository;
import com.mustafa_mert.backend.user.dto.UserResponse;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketPurchaseServiceImpl implements TicketPurchaseService {

    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    @Override
    public TicketPurchaseResponse purchaseTicket(PurchaseTicketRequest purchaseTicketRequest) {
        User currentUser = currentUserProvider.getCurrentUser();

        Event event = eventRepository.findById(purchaseTicketRequest.getEventId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.EVENT_NOT_FOUND)));

        if (event.getAvailableStock() < purchaseTicketRequest.getQuantity()) {
            throw new BaseException(new ErrorMessage(MessageType.NOT_ENOUGH_TICKETS));
        }

        event.setAvailableStock(event.getAvailableStock() - purchaseTicketRequest.getQuantity());
        eventRepository.save(event);

        BigDecimal totalPrice = event.getPrice()
                .multiply(BigDecimal.valueOf(purchaseTicketRequest.getQuantity()));

        TicketPurchase ticketPurchase = TicketPurchase.builder()
                .user(currentUser)
                .event(event)
                .quantity(purchaseTicketRequest.getQuantity())
                .totalPrice(totalPrice)
                .purchasedAt(LocalDateTime.now())
                .build();

        TicketPurchase savedTicketPurchase = ticketPurchaseRepository.save(ticketPurchase);

        EventResponse eventResponse = eventMapper.eventToEventResponse(event);

        UserResponse userResponse = UserResponse.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .lastName(currentUser.getLastName())
                .firstName(currentUser.getFirstName())
                .role(currentUser.getRole())
                .build();

        return TicketPurchaseResponse.builder()
                .id(savedTicketPurchase.getId())
                .event(eventResponse)
                .user(userResponse)
                .quantity(savedTicketPurchase.getQuantity())
                .totalPrice(savedTicketPurchase.getTotalPrice())
                .purchasedAt(savedTicketPurchase.getPurchasedAt())
                .build();
    }

    @Transactional
    @Override
    public void cancelTicket(Long id) {
        User currentUser = currentUserProvider.getCurrentUser();

        TicketPurchase ticketPurchase = ticketPurchaseRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.TICKET_PURCHASE_NOT_FOUND)));

        if (ticketPurchase.getEvent().getDateTime().isBefore(LocalDateTime.now())) {
            throw new BaseException(new ErrorMessage(MessageType.CANNOT_CANCEL_BEFORE_EVENT));
        }

        Event event = ticketPurchase.getEvent();
        event.setAvailableStock(event.getAvailableStock() + ticketPurchase.getQuantity());
        eventRepository.save(event);

        ticketPurchaseRepository.delete(ticketPurchase);
    }

    @Override
    public List<TicketPurchaseResponse> getAllPurchasedTickets() {
        User currentUser = currentUserProvider.getCurrentUser();

        List<TicketPurchase> ticketPurchases = ticketPurchaseRepository.findByUserId(currentUser.getId());
        if (ticketPurchases.isEmpty()) {
            throw new BaseException(new ErrorMessage(MessageType.TICKET_PURCHASE_NOT_FOUND));
        }

        List<TicketPurchaseResponse> ticketPurchaseResponses = new ArrayList<>();
        for (int i = 0; i < ticketPurchases.size(); i++) {
            TicketPurchase tp = ticketPurchases.get(i);

            Event event = tp.getEvent();

            EventResponse eventResponse = eventMapper.eventToEventResponse(event);

            User user = tp.getUser();

            UserResponse userResponse = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .lastName(user.getLastName())
                    .firstName(user.getFirstName())
                    .role(user.getRole())
                    .build();

            TicketPurchaseResponse ticketPurchaseResponse = TicketPurchaseResponse.builder()
                    .id(tp.getId())
                    .event(eventResponse)
                    .user(userResponse)
                    .quantity(tp.getQuantity())
                    .totalPrice(tp.getTotalPrice())
                    .purchasedAt(tp.getPurchasedAt())
                    .build();

            ticketPurchaseResponses.add(ticketPurchaseResponse);
        }

        return ticketPurchaseResponses;
    }
}
