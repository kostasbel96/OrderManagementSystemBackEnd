package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DriverInsertDTO {

    @NotNull(message = "Driver name is required")
    private String name;

    @NotNull(message = "Driver lastname is required")
    private String lastName;

    @NotNull(message = "Driver phone number is required")
    private String phoneNumber1;

    private String phoneNumber2;

}
