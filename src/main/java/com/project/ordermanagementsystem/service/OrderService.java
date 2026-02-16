package com.project.ordermanagementsystem.service;

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

import java.time.LocalDateTime;
import java.util.List;

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

        Order order = new Order();
        order.setAddress(dto.getAddress());
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
            item.setQuantity(itemDTO.getQuantity());

            order.addOrderItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        LOGGER.info("Order with id: {} saved successfully.", savedOrder.getId());

        return mapper.mapToOrderReadOnlyDTO(savedOrder);

    }

    @Transactional
    public Page<OrderReadOnlyDTO> getPaginatedOrders(int page, int size){
        String defaultSort = "id";

        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());

        return orderRepository.findAll(pageable).map(mapper::mapToOrderReadOnlyDTO);
    }

    @Transactional
    public ResponseDTO searchOrdersByCustomerName(String name,
                                                            String lastName){

        ResponseDTO response = new ResponseDTO();
        Specification<Order> spec = Specification
                .where(OrderSpecification.hasCustomerName(name))
                .or(OrderSpecification.hasCustomerLastName(lastName));

        List<Order> orders = orderRepository.findAll(spec);
        if (!orders.isEmpty()) {
            response.setOrderItems(orders.stream().map(mapper::mapToOrderReadOnlyDTO).toList());
            LOGGER.info("Orders found successfully.");
            return response;
        }

        response.setErrorResponse(new ErrorResponse("Orders not found."));
        LOGGER.error("Orders not found.");
        return response;

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

}
