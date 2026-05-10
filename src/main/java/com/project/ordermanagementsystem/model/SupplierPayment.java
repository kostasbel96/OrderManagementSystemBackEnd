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
@Table(name = "supplier_payments")
public class SupplierPayment extends AbstractEntity {

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

    @OneToMany(mappedBy = "supplierPayment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierPaymentOrder> supplierPaymentOrders = new ArrayList<>();

    public void addSupplierPaymentOrder(SupplierPaymentOrder paymentOrder) {
        supplierPaymentOrders.add(paymentOrder);
        paymentOrder.setSupplierPayment(this);
    }

    public void removeSupplierPaymentOrder(SupplierPaymentOrder paymentOrder) {
        supplierPaymentOrders.remove(paymentOrder);
        paymentOrder.setSupplierPayment(null);
    }
}