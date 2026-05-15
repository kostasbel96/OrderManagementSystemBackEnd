package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.exceptions.AppObjectInvalidQuantity;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.dto.OrderItemInsertDTO;
import com.project.ordermanagementsystem.dto.PurchaseOrderInsertDTO;
import com.project.ordermanagementsystem.dto.PurchaseOrderItemInsertDTO;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.*;
import com.project.ordermanagementsystem.repository.ProductRepository;
import com.project.ordermanagementsystem.repository.PurchaseOrderRepository;
import com.project.ordermanagementsystem.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.ordermanagementsystem.dto.PurchaseOrderReadOnlyDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderService.class);
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final Mapper mapper;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PurchaseOrderReadOnlyDTO savePurchaseOrder(PurchaseOrderInsertDTO dto) throws AppObjectNotFound {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new AppObjectNotFound(
                        "SupplierNotFound",
                        "Supplier with id: " + dto.getSupplierId() + " not found"
                ));

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setDate(LocalDate.now());
        order.setStatus(OrderStatus.PENDING);

        for (PurchaseOrderItemInsertDTO itemDTO : dto.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "ProductNotFound",
                            "Product with id: " + itemDTO.getProductId() + " not found"
                    ));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setPrice(new BigDecimal(itemDTO.getPrice()));
            item.setQuantity(itemDTO.getQuantity());

            order.addPurchaseOrderItem(item);
        }

        order.calculateTotalAmount();

        BigDecimal impact = order.getTotal();

        supplier.addToBalance(impact);

        supplierRepository.save(supplier);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        LOGGER.info("Purchase Order with id {} saved successfully", savedOrder.getId());

        return mapper.mapToPurchaseOrderReadOnlyDTO(savedOrder);
    }

}
