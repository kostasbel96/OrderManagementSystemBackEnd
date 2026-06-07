package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierInsertDTO {

    @NotNull(message = "Supplier name is required")
    private String name;

    @NotNull(message = "Supplier phone number is required")
    private String phoneNumber1;

    private String phoneNumber2;

    @NotNull(message = "Supplier VAT number is required")
    @Min(value = 10, message = "VAT number must be at least 10 characters long")
    private String vatNumber;

    @Email(message = "Supplier email is invalid")
    private String email;

    private String address;

}
