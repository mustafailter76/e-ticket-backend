package com.mustafa_mert.backend.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventResponse {
    private Long id;
    private String name;
    private String category;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private BigDecimal price;
    private Integer totalStock;
    private Integer availableStock;
    private String imageUrl;
}
