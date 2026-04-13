package com.project.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "customers")
public class Customer extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String lastName;

    private String email;

    private String phoneNumber1;

    private String phoneNumber2;

    private boolean active = true;

    private Double balance;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    public void addToBalance(Double amount) {
        if (balance == null) {
            balance = 0.0;
        }
        balance += amount;
        setBalance(balance);
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }

}
