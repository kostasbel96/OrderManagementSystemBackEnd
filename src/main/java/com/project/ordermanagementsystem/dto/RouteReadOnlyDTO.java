package com.project.ordermanagementsystem.dto;

import com.project.ordermanagementsystem.core.enums.RouteStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RouteReadOnlyDTO {

    private Long id;

    private List<OrderReadOnlyDTO> orders;

    private DriverReadOnlyDTO driver;

    private String notes;

    private String name;

    private String date;

    private RouteStatus status;


}
