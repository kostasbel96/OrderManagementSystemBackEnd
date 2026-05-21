package com.project.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "payments")
public class Payment extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String notes;

    private BigDecimal amount;

    private LocalDate date;

    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentPurchaseOrder> paymentOrders = new ArrayList<>();

    public void addPaymentPurchaseOrder(PaymentPurchaseOrder paymentPurchaseOrder) {
        paymentOrders.add(paymentPurchaseOrder);
        paymentPurchaseOrder.setPayment(this);
    }


}
