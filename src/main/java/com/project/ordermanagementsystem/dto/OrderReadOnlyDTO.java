package com.project.ordermanagementsystem.dto;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.enums.PaymentStatus;
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

    private OrderStatus status;

    private String paidAmount;

    private PaymentStatus paymentStatus;

    private String deposit;

    private Integer orderIndex;
}
