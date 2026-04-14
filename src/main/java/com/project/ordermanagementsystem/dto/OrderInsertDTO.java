package com.project.ordermanagementsystem.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderInsertDTO {

    @NotNull(message = "Order address is required.")
    private String address;

    @NotNull(message = "Customer id for order is required.")
    private Long customerId;

    @NotNull(message = "Order items is required.")
    @NotEmpty(message = "Order items is required.")
    private List<OrderItemInsertDTO> items;

    @Nullable
    private Double deposit;
}
