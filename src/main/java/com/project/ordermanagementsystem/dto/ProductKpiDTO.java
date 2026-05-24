package com.project.ordermanagementsystem.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductKpiDTO {

    private Long totalProducts;
    private Long totalProductsByDate;
    private Double deltaPercentage;
    private Long productLowStock;
    private Long deltaLowStockByYesterday;

}
