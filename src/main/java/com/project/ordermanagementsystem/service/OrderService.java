package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.CustomerSpecification;
import com.project.ordermanagementsystem.core.specifications.OrderSpecification;
import com.project.ordermanagementsystem.core.exceptions.AppObjectInvalidQuantity;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Order;
import com.project.ordermanagementsystem.model.OrderItem;
import com.project.ordermanagementsystem.model.Product;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final Mapper mapper;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderReadOnlyDTO saveOrder(OrderInsertDTO dto) throws AppObjectNotFound, AppObjectInvalidQuantity {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new AppObjectNotFound("CustomerNotFound","Customer not found"));

        String deposit = dto.getDeposit() != null && !dto.getDeposit().isEmpty() ? dto.getDeposit() : "0.0";
        Order order = new Order();
        order.setAddress(dto.getAddress());
        order.setDeposit(new BigDecimal(deposit));
        order.setCustomer(customer);
        order.setDate(LocalDateTime.now());

        for (OrderItemInsertDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new AppObjectNotFound("ProductNotFound","Product not found"));

            if (product.getQuantity() >= itemDTO.getQuantity()){
                product.reduceStock(itemDTO.getQuantity());
            } else {
                LOGGER.error("Product with id: {} has not enough quantity to place the order.", itemDTO.getProductId());
                throw new AppObjectInvalidQuantity("InvalidQuantity", "Product stock is not enough.");
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setPrice(new BigDecimal(itemDTO.getPrice()));
            item.setQuantity(itemDTO.getQuantity());

            order.addOrderItem(item);
        }
        order.calculateTotalAmount();
        customer.addToBalance(BigDecimal.valueOf(order.getTotal()).subtract(order.getDeposit()).toString());
        Order savedOrder = orderRepository.save(order);
        LOGGER.info("Order with id: {} saved successfully.", savedOrder.getId());

        return mapper.mapToOrderReadOnlyDTO(savedOrder);

    }

    @Transactional
    public Page<OrderReadOnlyDTO> getPaginatedOrders(int page, int size, String sortBy, String sortDirection){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Specification<Order> spec = Specification.where(OrderSpecification.isActive());
        return orderRepository.findAll(spec, pageable).map(mapper::mapToOrderReadOnlyDTO);
    }

    @Transactional
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

            existingOrder.calculateTotalAmount();
            BigDecimal oldTotal = BigDecimal.valueOf(existingOrder.getTotal());
            BigDecimal oldDeposit = existingOrder.getDeposit() != null
                    ? existingOrder.getDeposit()
                    : BigDecimal.ZERO;

            for (OrderItem oldItem : existingOrder.getItems()) {
                Product product = oldItem.getProduct();
                product.increaseStock(oldItem.getQuantity());
            }

            existingOrder.getItems().clear();

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
            String depositStr = Optional.ofNullable(dto.getDeposit())
                    .filter(s -> !s.isEmpty())
                    .orElse("0.0");

            BigDecimal newDeposit = new BigDecimal(depositStr);

            existingOrder.setAddress(dto.getAddress());
            existingOrder.setDeposit(newDeposit);

            existingOrder.calculateTotalAmount();
            BigDecimal newTotal = BigDecimal.valueOf(existingOrder.getTotal());

            BigDecimal diff = newTotal
                    .subtract(oldTotal)
                    .subtract(newDeposit.subtract(oldDeposit));

            Customer customer = existingOrder.getCustomer();
            customer.addToBalance(diff.toString());

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
        Order orderToDelete;
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            orderToDelete = orderRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("OrderNotFound",
                            String.format("Order with id: %s not found.", dto.getId())));
            if (!orderToDelete.getItems().isEmpty()) {
                orderToDelete.setActive(false);

                BigDecimal total = BigDecimal.valueOf(orderToDelete.getTotal());
                BigDecimal deposit = orderToDelete.getDeposit();

                Customer customer = orderToDelete.getCustomer();

                BigDecimal amountToRemove = total.subtract(deposit);

                customer.setBalance(
                        customer.getBalance().subtract(amountToRemove)
                );

                orderRepository.save(orderToDelete);
            } else {
                orderRepository.delete(orderToDelete);
            }
            OrderReadOnlyDTO returnedOrder = mapper.mapToOrderReadOnlyDTO(orderToDelete);
            responseDTO.setOrderReadOnlyDTO(returnedOrder);
            LOGGER.info("Order with id: {} deleted successfully.", returnedOrder.getId());
        } catch (AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }

        return responseDTO;
    }

}
