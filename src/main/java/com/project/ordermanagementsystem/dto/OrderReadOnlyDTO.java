package com.project.ordermanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderReadOnlyDTO {

    private Long id;

    private List<OrderItemReadOnlyDTO> items;

    private CustomerReadOnlyDTO customer;

    private String address;

    private String date;

    private String total;

    private String deposit;
}
