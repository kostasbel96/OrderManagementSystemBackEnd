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
@Table(name = "receipts")
public class Receipt extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String notes;

    private BigDecimal amount;

    private LocalDate date;

    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptOrder> receiptOrders = new ArrayList<>();

    public void addReceiptOrder(ReceiptOrder receiptOrder) {
        receiptOrders.add(receiptOrder);
        receiptOrder.setReceipt(this);
    }


}
