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
@Table(name = "purchase_orders")
public class PurchaseOrder extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    private LocalDate date;

    private boolean active = true;

    private BigDecimal total;

    @OneToMany(mappedBy = "purchaseOrder")
    private List<SupplierPaymentOrder> supplierPaymentOrders = new ArrayList<>();

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

    public void addPurchaseOrderItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
        calculateTotalAmount();
    }

    public void removePurchaseOrderItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
        calculateTotalAmount();
    }

    public void clearPurchaseOrderItems() {
        if (this.items != null) {
            this.items.forEach(item -> item.setPurchaseOrder(null));
            this.items.clear();
        }
    }
}