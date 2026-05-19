package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.exceptions.AppObjectInvalidQuantity;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
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
import org.springframework.validation.BindingResult;

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

    @Transactional(readOnly = true)
    public ResponseDTO getPurchaseOrderById(Long id) {
        PurchaseOrder order;
        ResponseDTO responseDTO = new ResponseDTO();
        try{
            order = purchaseOrderRepository.findById(id)
                    .orElseThrow(()-> new AppObjectNotFound("PurchaseOrderNotFound",
                            String.format("Purchase Order with id: %s not found", id)));
            LOGGER.info("Purchase Order with id: {} found successfully.", order.getId());
            responseDTO.setPurchaseOrderReadOnlyDTO(mapper.mapToPurchaseOrderReadOnlyDTO(order));
        } catch (AppObjectNotFound e){
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }
        return responseDTO;

    }

    @Transactional
    public ResponseDTO updatePurchaseOrder(PurchaseOrderUpdateDTO dto, BindingResult bindingResult) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {

            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            PurchaseOrder existingOrder = purchaseOrderRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "PurchaseOrderNotFound",
                            "Order with id: " + dto.getId() + " not found"
                    ));

            // OLD IMPACT
            BigDecimal oldTotal = existingOrder.getTotal();

            existingOrder.clearPurchaseOrderItems();

            // rebuild order
            for (OrderItemUpdateDTO itemDTO : dto.getItems()) {

                Product product = productRepository.findById(itemDTO.getProduct().getId())
                        .orElseThrow(() -> new AppObjectNotFound(
                                "ProductNotFound",
                                "Product not found"
                        ));

                if (itemDTO.getQuantity() < 1) {
                    throw new AppObjectInvalidQuantity(
                            "InvalidQuantity",
                            "Ivalid quantity"
                    );
                }

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setProduct(product);
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(new BigDecimal(itemDTO.getPrice()));

                existingOrder.addPurchaseOrderItem(item);
            }

            OrderStatus oldStatus = existingOrder.getStatus();

            if (oldStatus != OrderStatus.DELIVERED
                    && dto.getStatus() == OrderStatus.DELIVERED) {
                existingOrder.getItems().forEach(item -> {
                    Product product = item.getProduct();
                    product.increaseStock(item.getQuantity());
                    productRepository.save(product);
                });
            } else if (oldStatus == OrderStatus.DELIVERED
                    && dto.getStatus() != OrderStatus.DELIVERED) {
                existingOrder.getItems().forEach(item -> {
                    item.getProduct().decreaseStock(item.getQuantity());
                    productRepository.save(item.getProduct());
                });
            }

            existingOrder.setStatus(dto.getStatus());

            existingOrder.calculateTotalAmount();
            BigDecimal newTotal = existingOrder.getTotal();

            BigDecimal diff = newTotal.subtract(oldTotal);

            Supplier supplier = existingOrder.getSupplier();
            supplier.addToBalance(diff);

            supplierRepository.save(supplier);
            PurchaseOrder updatedOrder = purchaseOrderRepository.save(existingOrder);

            responseDTO.setPurchaseOrderReadOnlyDTO(
                    mapper.mapToPurchaseOrderReadOnlyDTO(updatedOrder)
            );

            LOGGER.info("Purchase Order with id {} updated successfully", updatedOrder.getId());

        } catch (AppObjectNotFound | ValidationException | AppObjectInvalidQuantity e) {

            LOGGER.error(e.getMessage());

            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }

    @Transactional
    public ResponseDTO deletePurchaseOrder(PurchaseOrderUpdateDTO dto) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {

            PurchaseOrder order = purchaseOrderRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "PurchaseOrderNotFound",
                            "Order with id: " + dto.getId() + " not found"
                    ));

            if (!order.isActive()) {
                throw new IllegalStateException("Purchase Order already cancelled");
            }

            // calculate current impact
            order.calculateTotalAmount();
            BigDecimal oldImpact = order.getTotal();

            // SOFT DELETE
            order.setActive(false);

            if (order.getStatus() == OrderStatus.DELIVERED) {
                // STOCK RESTORE
                order.getItems().forEach(item -> {
                    item.getProduct().decreaseStock(item.getQuantity());
                    productRepository.save(item.getProduct());
                });
            } else {
                // BALANCE REVERSAL
                Supplier supplier = order.getSupplier();
                supplier.addToBalance(oldImpact.negate());
                supplierRepository.save(supplier);
            }

            PurchaseOrder saved = purchaseOrderRepository.save(order);

            responseDTO.setPurchaseOrderReadOnlyDTO(
                    mapper.mapToPurchaseOrderReadOnlyDTO(saved)
            );

            LOGGER.info("Purchase Order with id {} cancelled successfully", saved.getId());

        } catch (AppObjectNotFound | IllegalStateException e) {

            LOGGER.error(e.getMessage());

            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }
}
