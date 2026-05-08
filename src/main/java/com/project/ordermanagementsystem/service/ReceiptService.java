package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.ReceiptSpecification;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Order;
import com.project.ordermanagementsystem.model.Receipt;
import com.project.ordermanagementsystem.model.ReceiptOrder;
import com.project.ordermanagementsystem.repository.CustomerRepository;
import com.project.ordermanagementsystem.repository.OrderRepository;
import com.project.ordermanagementsystem.repository.ReceiptRepository;
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
public class ReceiptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiptService.class);
    private final Mapper mapper;
    private final ReceiptRepository receiptRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ResponseDTO saveReceipt(ReceiptInsertDTO dto, BindingResult bindingResult) {

        ResponseDTO responseDTO = new ResponseDTO();
        try {
            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            Receipt receipt = new Receipt();

            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new AppObjectNotFound("CustomerNotFound", "Customer not found"));

            BigDecimal amount = new BigDecimal(dto.getAmount());
            receipt.setAmount(amount);
            receipt.setNotes(dto.getNotes());
            receipt.setDate(LocalDate.now());
            receipt.setCustomer(customer);
            customer.addToBalance(amount.negate());

            if (dto.getOrderIds() != null && !dto.getOrderIds().isEmpty()) {
                List<Order> orders = orderRepository.findAllById(dto.getOrderIds());

                BigDecimal remainingAmount = amount;

                for (Order order : orders) {
                    if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }

                    BigDecimal orderRemaining = order.getRemainingAmount();
                    BigDecimal paymentForOrder = remainingAmount.compareTo(orderRemaining) > 0
                            ? orderRemaining
                            : remainingAmount;

                    order.addPayment(paymentForOrder);
                    remainingAmount = remainingAmount.subtract(paymentForOrder);

                    ReceiptOrder receiptOrder = new ReceiptOrder();
                    receiptOrder.setOrder(order);
                    receiptOrder.setDate(receipt.getDate());
                    receiptOrder.setCreatedAt(LocalDateTime.now());
                    receiptOrder.setAmount(paymentForOrder);

                    receipt.addReceiptOrder(receiptOrder);
                }

                orderRepository.saveAll(orders);
            }

            customerRepository.save(customer);
            Receipt savedReceipt = receiptRepository.save(receipt);
            responseDTO.setReceiptReadOnlyDTO(mapper.mapToReceiptReadOnlyDTO(savedReceipt));
            LOGGER.info("Receipt with id: {} saved successfully.", savedReceipt.getId());

        } catch (ValidationException | AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }

        return responseDTO;
    }

    @Transactional
    public ResponseDTO getReceiptById(Long id) {
        Receipt receipt;
        ResponseDTO responseDTO = new ResponseDTO();
        try{
            receipt = receiptRepository.findById(id)
                    .orElseThrow(()-> new AppObjectNotFound("ReceiptNotFound",String.format("Receipt with id: %s not found", id)));
            LOGGER.info("Receipt with id: {} found successfully.", receipt.getId());
            responseDTO.setReceiptReadOnlyDTO(mapper.mapToReceiptReadOnlyDTO(receipt));
        } catch (AppObjectNotFound e){
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }
        return responseDTO;

    }

    public Page<ReceiptReadOnlyDTO> searchReceipts(SearchRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(
                        Sort.Direction.fromString(request.getSort().getSort()),
                        request.getSort().getField()
                )
        );

        Specification<Receipt> spec = Specification.where(ReceiptSpecification.isActive());

        if (request.getGlobalSearch() != null && !request.getGlobalSearch().isBlank()) {
            spec = spec.and(ReceiptSpecification.globalSearch(request.getGlobalSearch()));
        }

        if (request.getFilters() != null) {
            for (FilterRequest filter : request.getFilters()) {
                spec = spec.and(ReceiptSpecification.fromFilter(filter));
            }
        }

        Page<Receipt> receiptPage = receiptRepository.findAll(spec, pageable);

        return receiptPage.map(mapper::mapToReceiptReadOnlyDTO);
    }

    @Transactional
    public ResponseDTO deleteReceipt(Long id) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {
            Receipt receipt = receiptRepository.findById(id)
                    .orElseThrow(() -> new AppObjectNotFound(
                            "ReceiptNotFound",
                            "Receipt with id: " + id + " not found"
                    ));

            // SOFT DELETE
            receipt.setActive(false);

            // BALANCE REVERSAL
            Customer customer = receipt.getCustomer();
            customer.addToBalance(receipt.getAmount()); // Προσθέτουμε πίσω το ποσό

            // REVERSE PAYMENTS
            for (ReceiptOrder ro : receipt.getReceiptOrders()) {
                Order order = ro.getOrder();

                BigDecimal paidToReverse = ro.getAmount();
                order.setPaidAmount(order.getPaidAmount().subtract(paidToReverse));
                order.updatePaymentStatus();
                orderRepository.save(order);
            }

            customerRepository.save(customer);
            Receipt savedReceipt = receiptRepository.save(receipt);
            responseDTO.setReceiptReadOnlyDTO(
                    mapper.mapToReceiptReadOnlyDTO(savedReceipt)
            );

            LOGGER.info("Receipt with id {} deleted successfully", savedReceipt.getId());

        } catch (AppObjectNotFound | IllegalStateException e) {
            LOGGER.error(e.getMessage());
            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }

}
