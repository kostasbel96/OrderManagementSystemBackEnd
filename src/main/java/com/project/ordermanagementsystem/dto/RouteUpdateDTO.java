package com.project.ordermanagementsystem.dto;

import com.project.ordermanagementsystem.core.enums.RouteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RouteUpdateDTO {

    @NotNull(message = "Order id is required.")
    private Long id;

    @NotBlank(message = "Route name is required.")
    private String name;

    private String notes;

    @NotNull(message = "Driver for route is required.")
    private Long driverId;

    private RouteStatus status;

    @NotNull(message = "Orders for route is required.")
    private List<Long> orderIds;

    @NotNull(message = "Execution date is required.")
    private LocalDate date;

}
