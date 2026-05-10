package com.project.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "customers")
public class Customer extends Person {

    private String name;

    private String lastName;

    private String email;

    private BigDecimal balance = BigDecimal.valueOf(0.0);

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "customer")
    private List<Receipt> receipts;

    public void addToBalance(BigDecimal amount) {
        if (balance == null) {
            balance = new BigDecimal("0.0");
        }
        balance = balance.add(amount);
    }

}
