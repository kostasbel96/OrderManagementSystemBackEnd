package com.project.ordermanagementsystem.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerUpdateDTO {

    @NotNull(message = "Customer Id is required.")
    private Long id;

    @NotEmpty(message = "Customer name is required.")
    private String name;

    @NotEmpty(message = "Customer lastname is required.")
    private String lastName;

    @Nullable
    @Email
    private String email;

    @NotBlank(message = "Customer phone number is required.")
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
