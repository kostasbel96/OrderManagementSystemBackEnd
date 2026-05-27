package com.project.ordermanagementsystem.model;

import com.project.ordermanagementsystem.core.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "routes")
public class Route extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String notes;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private RouteStatus status; // PLANNED, IN_PROGRESS, COMPLETED, CANCELLED

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private DriverPerson driver;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC NULLS LAST")
    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order){
        orders.add(order);
        order.setRoute(this);
    }

    public void clearOrders() {
        orders.forEach(order -> order.setRoute(null));
        orders.clear();
    }
}
