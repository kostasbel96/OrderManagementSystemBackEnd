package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SupplierUpdateDTO {

    @NotNull
    private Long id;

    @NotEmpty
    @NotNull
    private String name;

    private String address;

    @NotNull
    @Min(value = 10, message = "Phone number must be at least 10 digits.")
    private String phoneNumber1;


    private String phoneNumber2;

    @Email
    private String email;

    @NotNull
    @Min(value = 10, message = "VAT number must be at least 10 characters long.")
    private String vatNumber;

}
