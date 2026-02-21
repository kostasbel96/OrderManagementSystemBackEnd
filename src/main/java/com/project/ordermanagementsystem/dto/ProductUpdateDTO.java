package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class ProductUpdateDTO {

    @NotNull(message = "Product Id is required.")
    private Long id;

    @NotNull(message = "Product name is required.")
    @NotBlank(message = "Product name is required.")
    private String name;

    @NotNull(message = "Product description is required.")
    @NotBlank(message = "Product description is required.")
    private String description;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Product quantity must be at least 1.")
    private Integer quantity;

}
