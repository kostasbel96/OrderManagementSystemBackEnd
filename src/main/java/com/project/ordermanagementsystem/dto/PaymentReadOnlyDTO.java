package com.project.ordermanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentReadOnlyDTO {

    private Long id;

    private String date;

    private SupplierReadOnlyDTO supplier;

    private String amount;

    private String notes;

}
