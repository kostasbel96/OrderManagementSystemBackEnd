package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RouteInsertDTO {

    @NotNull(message = "Route name is required.")
    private String name;

    private String notes;

    @NotNull(message = "Driver for route is required.")
    private Long driverId;

    @NotNull(message = "Orders for route is required.")
    @NotEmpty(message = "Orders for route is required.")
    private List<RouteOrderDTO> orderIds;

    @NotNull(message = "Execution date is required.")
    private LocalDate date;

}
