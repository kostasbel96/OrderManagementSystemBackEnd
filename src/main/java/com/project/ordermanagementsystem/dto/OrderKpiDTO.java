package com.project.ordermanagementsystem.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderKpiDTO {

    private Long totalOrdersByDate;
    private Long deltaOrdersByYesterday;

}
