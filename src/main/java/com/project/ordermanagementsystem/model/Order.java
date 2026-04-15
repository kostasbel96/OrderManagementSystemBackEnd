package com.project.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "orders")
public class Order extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime date;

    private boolean active = true;

    private BigDecimal deposit;

    @Transient
    public Double getTotalAmount() {
        return items.stream()
                .mapToDouble(item ->
                    {
                        if (item != null && item.getPrice() != null){
                            return item.getPrice().doubleValue() * item.getQuantity();
                        }
                        return 0.0;
                    }
                )
                .sum();
    }

    public void addOrderItem(OrderItem item){
        items.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public void clearOrderItems() {
        if (this.items != null) {
            this.items.forEach(orderItem -> orderItem.setOrder(null));
            this.items.clear();
        }
    }

}
