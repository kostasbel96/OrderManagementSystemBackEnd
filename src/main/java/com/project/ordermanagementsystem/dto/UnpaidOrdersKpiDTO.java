package com.project.ordermanagementsystem.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UnpaidOrdersKpiDTO {

    private Long totalOrders;
    private Long deltaUnpaidOrdersByYesterday;

}
