package com.mustafa_mert.backend.event.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SalesDashboardResponse {

    private Integer totalEventCount;

    private Integer totalSoldTickets;

    private BigDecimal totalRevenue;

    private List<EventSalesResponse> events;
}