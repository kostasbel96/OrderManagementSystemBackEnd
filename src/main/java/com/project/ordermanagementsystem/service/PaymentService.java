package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.PaymentSpecification;
import com.project.ordermanagementsystem.core.specifications.ReceiptSpecification;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.*;
import com.project.ordermanagementsystem.repository.*;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    private final Mapper mapper;
    private final PaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional
    public ResponseDTO savePayment(PaymentInsertDTO dto, BindingResult bindingResult) {

        ResponseDTO responseDTO = new ResponseDTO();
        try {
            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            Payment payment = new Payment();

            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new AppObjectNotFound("SupplierNotFound", "Supplier not found"));

            BigDecimal amount = new BigDecimal(dto.getAmount());
            payment.setAmount(amount);
            payment.setNotes(dto.getNotes());
            payment.setDate(LocalDate.now());
            payment.setSupplier(supplier);
            supplier.addToBalance(amount.negate());

            if (dto.getOrderIds() != null && !dto.getOrderIds().isEmpty()) {
                List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAllById(dto.getOrderIds());

                BigDecimal remainingAmount = amount;

                for (PurchaseOrder order : purchaseOrders) {
                    if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }

                    BigDecimal orderRemaining = order.getRemainingAmount();
                    BigDecimal paymentForOrder = remainingAmount.compareTo(orderRemaining) > 0
                            ? orderRemaining
                            : remainingAmount;

                    order.addPayment(paymentForOrder);
                    remainingAmount = remainingAmount.subtract(paymentForOrder);

                    PaymentPurchaseOrder paymentPurchaseOrder = new PaymentPurchaseOrder();
                    paymentPurchaseOrder.setPurchaseOrder(order);
                    paymentPurchaseOrder.setDate(payment.getDate());
                    paymentPurchaseOrder.setCreatedAt(LocalDateTime.now());
                    paymentPurchaseOrder.setAmount(paymentForOrder);

                    payment.addPaymentPurchaseOrder(paymentPurchaseOrder);
                }

                purchaseOrderRepository.saveAll(purchaseOrders);
            }

            supplierRepository.save(supplier);
            Payment savedPayment = paymentRepository.save(payment);
            responseDTO.setPaymentReadOnlyDTO(mapper.mapToPaymentReadOnlyDTO(savedPayment));
            LOGGER.info("Payment with id: {} saved successfully.", savedPayment.getId());

        } catch (ValidationException | AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }

        return responseDTO;
    }

    @Transactional
    public ResponseDTO getPaymentById(Long id) {
        Payment payment;
        ResponseDTO responseDTO = new ResponseDTO();
        try{
            payment = paymentRepository.findById(id)
                    .orElseThrow(()-> new AppObjectNotFound("PaymentNotFound",String.format("Payment with id: %s not found", id)));
            LOGGER.info("Payment with id: {} found successfully.", payment.getId());
            responseDTO.setPaymentReadOnlyDTO(mapper.mapToPaymentReadOnlyDTO(payment));
        } catch (AppObjectNotFound e){
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }
        return responseDTO;

    }

    public Page<PaymentReadOnlyDTO> searchPayments(SearchRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(
                        Sort.Direction.fromString(request.getSort().getSort()),
                        request.getSort().getField()
                )
        );

        Specification<Payment> spec = Specification.where(PaymentSpecification.isActive());

        if (request.getGlobalSearch() != null && !request.getGlobalSearch().isBlank()) {
            spec = spec.and(PaymentSpecification.globalSearch(request.getGlobalSearch()));
        }

        if (request.getFilters() != null) {
            for (FilterRequest filter : request.getFilters()) {
                spec = spec.and(PaymentSpecification.fromFilter(filter));
            }
        }

        Page<Payment> paymentPage = paymentRepository.findAll(spec, pageable);

        return paymentPage.map(mapper::mapToPaymentReadOnlyDTO);
    }

    @Transactional
    public ResponseDTO deletePayment(Long id) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new AppObjectNotFound(
                            "PaymentNotFound",
                            "Payment with id: " + id + " not found"
                    ));

            // SOFT DELETE
            payment.setActive(false);

            // BALANCE REVERSAL
            Supplier supplier = payment.getSupplier();
            supplier.addToBalance(payment.getAmount());

            // REVERSE PAYMENTS
            for (PaymentPurchaseOrder po : payment.getPaymentOrders()) {
                PurchaseOrder order = po.getPurchaseOrder();

                if (order.getStatus() != OrderStatus.DELIVERED){
                    BigDecimal paidToReverse = po.getAmount();
                    order.setPaidAmount(order.getPaidAmount().subtract(paidToReverse));
                    order.updatePaymentStatus();
                    purchaseOrderRepository.save(order);
                }

            }

            supplierRepository.save(supplier);
            Payment savedPayment = paymentRepository.save(payment);
            responseDTO.setPaymentReadOnlyDTO(
                    mapper.mapToPaymentReadOnlyDTO(savedPayment)
            );

            LOGGER.info("Payment with id {} deleted successfully", savedPayment.getId());

        } catch (AppObjectNotFound | IllegalStateException e) {
            LOGGER.error(e.getMessage());
            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }

}
