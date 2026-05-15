package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PurchaseOrderItemInsertDTO {

    @NotNull(message = "Product id must not be empty.")
    private Long productId;

    @NotNull(message = "Quantity must not be empty.")
    private Integer quantity;

    @NotNull(message = "Price must not be empty.")
    private String price;

}
