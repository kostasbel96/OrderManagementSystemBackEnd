package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DriverUpdateDTO {

    @NotNull(message = "Driver Id is required.")
    private Long id;

    @NotEmpty(message = "Driver name is required.")
    private String name;

    @NotEmpty(message = "Driver lastname is required.")
    private String lastName;

    @NotBlank(message = "Driver phone number is required.")
    @Pattern(
            regexp = "^\\+?[0-9]{10,}$",
            message = "Phone number must be at least 10 digits and contain only numbers."
    )
    private String phoneNumber1;

    @Pattern(
            regexp = "^(|\\+?[0-9]{10,})$",
            message = "Phone number must be empty or at least 10 digits"
    )
    private String phoneNumber2;
}
