package com.project.ordermanagementsystem.dto;

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
public class PurchaseOrderInsertDTO {

    @NotNull(message = "Supplier id must not be empty.")
    private Long supplierId;

    @NotEmpty(message = "Order items must not be empty.")
    private List<PurchaseOrderItemInsertDTO> items;

}
