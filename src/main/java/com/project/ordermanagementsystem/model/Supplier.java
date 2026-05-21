package com.project.ordermanagementsystem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "suppliers")
public class Supplier extends Person {

    private String name;

    private String email;

    private String vatNumber;

    private String address;

    private BigDecimal balance = BigDecimal.ZERO;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrder> purchaseOrders = new ArrayList<>();

    @OneToMany(mappedBy = "supplier")
    private List<Payment> supplierPayments = new ArrayList<>();

    public void addToBalance(BigDecimal amount) {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        balance = balance.add(amount);
    }

}