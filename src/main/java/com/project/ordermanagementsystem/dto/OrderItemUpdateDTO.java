package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemUpdateDTO {

    @NotNull(message = "OrderItem id is required.")
    private Long id;

    @NotNull(message = "Product is required.")
    private ProductUpdateDTO product;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Product quantity must be at least 1.")
    private Integer quantity;

}
