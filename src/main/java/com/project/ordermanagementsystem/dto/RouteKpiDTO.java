package com.project.ordermanagementsystem.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RouteKpiDTO {

    private Long totalRoutesByDate;
    private Long deltaRoutesByYesterday;

}
