package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.OrderSpecification;
import com.project.ordermanagementsystem.core.exceptions.AppObjectInvalidQuantity;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.*;
import com.project.ordermanagementsystem.repository.CustomerRepository;
import com.project.ordermanagementsystem.repository.OrderRepository;
import com.project.ordermanagementsystem.repository.ProductRepository;
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
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final Mapper mapper;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderReadOnlyDTO saveOrder(OrderInsertDTO dto)
            throws AppObjectNotFound, AppObjectInvalidQuantity {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new AppObjectNotFound(
                        "CustomerNotFound",
                        "Customer not found"
                ));

        Order order = new Order();
        order.setAddress(dto.getAddress());
        order.setCustomer(customer);
        order.setDate(LocalDate.now());
        order.setStatus(OrderStatus.PENDING);

        for (OrderItemInsertDTO itemDTO : dto.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "ProductNotFound",
                            "Product not found"
                    ));

            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new AppObjectInvalidQuantity(
                        "InvalidQuantity",
                        "Product stock is not enough"
                );
            }

            product.reduceStock(itemDTO.getQuantity());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setPrice(new BigDecimal(itemDTO.getPrice()));
            item.setQuantity(itemDTO.getQuantity());

            order.addOrderItem(item);
        }

        order.calculateTotalAmount();

        BigDecimal impact = order.getTotal();

        customer.addToBalance(impact);

        customerRepository.save(customer);
        Order savedOrder = orderRepository.save(order);

        LOGGER.info("Order with id {} saved successfully", savedOrder.getId());

        return mapper.mapToOrderReadOnlyDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderReadOnlyDTO> getPaginatedOrders(int page, int size, String sortBy, String sortDirection){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Specification<Order> spec = Specification.where(OrderSpecification.isActive());
        return orderRepository.findAll(spec, pageable).map(mapper::mapToOrderReadOnlyDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderReadOnlyDTO> searchOrders(SearchRequest request){

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(
                        Sort.Direction.fromString(request.getSort().getSort()),
                        request.getSort().getField()
                )
        );

        Specification<Order> spec = Specification.where(OrderSpecification.isActive());

        if (request.getGlobalSearch() != null && !request.getGlobalSearch().isBlank()) {
            spec = spec.and(OrderSpecification.globalSearch(request.getGlobalSearch()));
        }

        if (request.getFilters() != null) {
            for (FilterRequest filter : request.getFilters()) {
                spec = spec.and(OrderSpecification.fromFilter(filter));
            }
        }

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        return orderPage.map(mapper::mapToOrderReadOnlyDTO);

    }

    @Transactional
    public ResponseDTO getOrderById(Long id) {
        Order order;
        ResponseDTO responseDTO = new ResponseDTO();
        try{
            order = orderRepository.findById(id)
                    .orElseThrow(()-> new AppObjectNotFound("OrderNotFound",String.format("Order with id: %s not found", id)));
            LOGGER.info("Order with id: {} found successfully.", order.getId());
            responseDTO.setOrderReadOnlyDTO(mapper.mapToOrderReadOnlyDTO(order));
        } catch (AppObjectNotFound e){
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }
        return responseDTO;

    }

    @Transactional
    public ResponseDTO updateOrder(OrderUpdateDTO dto, BindingResult bindingResult) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {

            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            Order existingOrder = orderRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "OrderNotFound",
                            "Order with id: " + dto.getId() + " not found"
                    ));

            // OLD IMPACT
            BigDecimal oldTotal = existingOrder.getTotal();

            // restore stock
            for (OrderItem oldItem : existingOrder.getItems()) {
                Product product = oldItem.getProduct();
                product.increaseStock(oldItem.getQuantity());
            }

            existingOrder.getItems().clear();

            // rebuild order
            for (OrderItemUpdateDTO itemDTO : dto.getItems()) {

                Product product = productRepository.findById(itemDTO.getProduct().getId())
                        .orElseThrow(() -> new AppObjectNotFound(
                                "ProductNotFound",
                                "Product not found"
                        ));

                if (product.getQuantity() < itemDTO.getQuantity()) {
                    throw new AppObjectInvalidQuantity(
                            "InvalidQuantity",
                            "Product stock is not enough"
                    );
                }

                product.reduceStock(itemDTO.getQuantity());

                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(new BigDecimal(itemDTO.getPrice()));

                existingOrder.addOrderItem(item);
            }

            existingOrder.setAddress(dto.getAddress());
            existingOrder.setStatus(dto.getStatus());
            // NEW IMPACT
            existingOrder.calculateTotalAmount();
            BigDecimal newTotal = existingOrder.getTotal();

            // BALANCE DIFF (IMPORTANT PART)
            BigDecimal diff = newTotal.subtract(oldTotal);

            Customer customer = existingOrder.getCustomer();
            customer.addToBalance(diff);

            customerRepository.save(customer);
            Order updatedOrder = orderRepository.save(existingOrder);

            responseDTO.setOrderReadOnlyDTO(
                    mapper.mapToOrderReadOnlyDTO(updatedOrder)
            );

            LOGGER.info("Order with id {} updated successfully", updatedOrder.getId());

        } catch (AppObjectNotFound | ValidationException | AppObjectInvalidQuantity e) {

            LOGGER.error(e.getMessage());

            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }

    @Transactional
    public ResponseDTO deleteOrder(OrderUpdateDTO dto) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {

            Order order = orderRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "OrderNotFound",
                            "Order with id: " + dto.getId() + " not found"
                    ));

            if (!order.isActive()) {
                throw new IllegalStateException("Order already cancelled");
            }

            // calculate current impact
            order.calculateTotalAmount();
            BigDecimal oldImpact = order.getTotal();

            // SOFT DELETE
            order.setActive(false);

            // BALANCE REVERSAL
            Customer customer = order.getCustomer();
            customer.addToBalance(oldImpact.negate());

            customerRepository.save(customer);
            Order saved = orderRepository.save(order);

            responseDTO.setOrderReadOnlyDTO(
                    mapper.mapToOrderReadOnlyDTO(saved)
            );

            LOGGER.info("Order with id {} cancelled successfully", saved.getId());

        } catch (AppObjectNotFound | IllegalStateException e) {

            LOGGER.error(e.getMessage());

            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }

}
