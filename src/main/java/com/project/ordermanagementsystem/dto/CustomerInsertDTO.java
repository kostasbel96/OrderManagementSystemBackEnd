package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerInsertDTO {

    @NotNull(message = "Customer name is required")
    private String name;

    @NotNull(message = "Customer lastname is required")
    private String lastName;

    @NotNull(message = "Customer phone number is required")
    private String phoneNumber1;

    private String phoneNumber2;

    @Email(message = "Customer email is invalid")
    private String email;

}
