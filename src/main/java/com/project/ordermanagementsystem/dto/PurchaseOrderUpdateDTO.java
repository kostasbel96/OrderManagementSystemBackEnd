package com.project.ordermanagementsystem.dto;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderUpdateDTO {

    @NotNull(message = "Purchase Order id is required.")
    private Long id;

    @NotNull(message = "Purchase Order items is required.")
    @NotEmpty(message = "Purchase Order items is required.")
    private List<OrderItemUpdateDTO> items;

    @NotNull(message = "Supplier is required")
    private SupplierUpdateDTO supplier;

    private OrderStatus status;
}
