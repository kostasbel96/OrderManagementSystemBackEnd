package com.project.ordermanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductReadOnlyDTO {

    private Long id;
    private String name;
    private String description;
    private Integer quantity;

}
