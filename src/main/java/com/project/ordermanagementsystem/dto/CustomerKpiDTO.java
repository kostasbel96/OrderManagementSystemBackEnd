package com.project.ordermanagementsystem.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomerKpiDTO {

    private Long totalCustomers;
    private Long deltaCustomersByYesterday;

}
