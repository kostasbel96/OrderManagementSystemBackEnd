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
@Table(name = "products")
public class Product extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;

    private Integer quantity;

    private boolean active = true;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> items = new ArrayList<>();

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (this.quantity < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        this.quantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.quantity = this.quantity + quantity;
    }

}
