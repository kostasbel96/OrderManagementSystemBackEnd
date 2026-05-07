package com.project.ordermanagementsystem.model;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    private LocalDate date;

    private boolean active = true;

    private BigDecimal total;

    @OneToMany(mappedBy = "order")
    private List<ReceiptOrder> receiptOrders = new ArrayList<>();

    public void calculateTotalAmount() {
        this.total = items == null ? BigDecimal.ZERO :
                items.stream()
                        .filter(i -> i != null && i.getPrice() != null)
                        .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        updatePaymentStatus();
    }

    public BigDecimal getRemainingAmount() {
        BigDecimal totalAmount = total != null ? total : BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return totalAmount.subtract(paid);
    }

    public void addPayment(BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal remaining = getRemainingAmount();
        BigDecimal actualPayment = paymentAmount.compareTo(remaining) > 0 ? remaining : paymentAmount;

        this.paidAmount = this.paidAmount.add(actualPayment);
        updatePaymentStatus();
    }

    public void updatePaymentStatus() {
        BigDecimal remaining = getRemainingAmount();

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            this.paymentStatus = PaymentStatus.PAID;
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.paymentStatus = PaymentStatus.PARTIAL;
        } else {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
    }

    public void addOrderItem(OrderItem item){
        items.add(item);
        item.setOrder(this);
        updatePaymentStatus();
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
