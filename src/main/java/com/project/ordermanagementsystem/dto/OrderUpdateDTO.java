package com.project.ordermanagementsystem.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderUpdateDTO {

    @NotNull(message = "Order id is required.")
    private Long id;

    @NotNull(message = "Order adress is required.")
    private String address;

    @NotNull(message = "Order items is required.")
    @NotEmpty(message = "Order items is required.")
    private List<OrderItemUpdateDTO> items;

    @NotNull(message = "Customer is required")
    private CustomerUpdateDTO customer;

    @Nullable
    private String deposit;
}
