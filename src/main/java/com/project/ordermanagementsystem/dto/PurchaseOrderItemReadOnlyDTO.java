package com.project.ordermanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PurchaseOrderItemReadOnlyDTO {

    private Long id;

    private ProductReadOnlyDTO product;

    private Integer quantity;

    private String price;

}
