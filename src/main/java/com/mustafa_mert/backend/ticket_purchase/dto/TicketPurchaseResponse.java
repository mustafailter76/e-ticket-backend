package com.mustafa_mert.backend.ticket_purchase.dto;

import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.user.dto.UserResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TicketPurchaseResponse {
    private Long id;
    private UserResponse user;
    private EventResponse event;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime purchasedAt;
}
