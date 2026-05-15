package com.mustafa_mert.backend.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateEventRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer totalStock;

    @NotNull
    @Future
    private LocalDateTime dateTime;
}