package com.mustafa_mert.backend.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class EventSalesResponse {

    private Long eventId;

    private String eventName;

    private String category;

    private Integer totalStock;

    private Integer availableStock;

    private Integer soldTickets;

    private BigDecimal totalRevenue;

    private String imageUrl;
}