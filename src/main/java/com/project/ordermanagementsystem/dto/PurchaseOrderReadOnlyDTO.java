package com.project.ordermanagementsystem.dto;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PurchaseOrderReadOnlyDTO {

    private Long id;

    private SupplierReadOnlyDTO supplier;

    private List<PurchaseOrderItemReadOnlyDTO> items;

    private String total;

    private OrderStatus status;

    private PaymentStatus paymentStatus;

    private String paidAmount;

    private String date;

}
