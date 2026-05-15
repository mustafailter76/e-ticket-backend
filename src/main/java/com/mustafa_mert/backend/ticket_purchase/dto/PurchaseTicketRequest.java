package com.mustafa_mert.backend.ticket_purchase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseTicketRequest {

    @NotNull
    private Long eventId;

    @NotNull
    @Min(1)
    @Max(3)
    private Integer quantity;
}