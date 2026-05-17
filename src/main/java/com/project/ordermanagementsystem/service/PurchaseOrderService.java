package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.specifications.OrderSpecification;
import com.project.ordermanagementsystem.core.specifications.PurchaseOrderSpecification;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.*;
import com.project.ordermanagementsystem.repository.ProductRepository;
import com.project.ordermanagementsystem.repository.PurchaseOrderRepository;
import com.project.ordermanagementsystem.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public Page<PurchaseOrderReadOnlyDTO> searchPurchaseOrders(SearchRequest request){

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(
                        Sort.Direction.fromString(request.getSort().getSort()),
                        request.getSort().getField()
                )
        );

        Specification<PurchaseOrder> spec = Specification.where(PurchaseOrderSpecification.isActive());

        if (request.getGlobalSearch() != null && !request.getGlobalSearch().isBlank()) {
            spec = spec.and(PurchaseOrderSpecification.globalSearch(request.getGlobalSearch()));
        }

        if (request.getFilters() != null) {
            for (FilterRequest filter : request.getFilters()) {
                spec = spec.and(PurchaseOrderSpecification.fromFilter(filter));
            }
        }

        Page<PurchaseOrder> purchaseOrderPage = purchaseOrderRepository.findAll(spec, pageable);

        return purchaseOrderPage.map(mapper::mapToPurchaseOrderReadOnlyDTO);

    }

}
