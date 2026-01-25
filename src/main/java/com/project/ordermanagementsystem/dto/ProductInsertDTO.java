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
public class ProductInsertDTO {

    @NotNull(message = "Product name field is required")
    private String name;

    @NotNull(message = "Product description field is required")
    private String description;

    @NotNull(message = "Product quantity field is required")
    private Integer quantity;
}
