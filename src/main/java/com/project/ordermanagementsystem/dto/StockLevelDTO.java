package com.project.ordermanagementsystem.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class StockLevelDTO {

    private String productName;

    private Integer quantity;

    private Integer pct;

}
