package com.project.ordermanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierReadOnlyDTO {

    private Long id;

    private String name;

    private String address;

    private String phoneNumber1;

    private String phoneNumber2;

    private String email;

    private String vatNumber;

    private String balance;
}
