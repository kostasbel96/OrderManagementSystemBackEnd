package com.project.ordermanagementsystem.mapper;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Order;
import com.project.ordermanagementsystem.model.OrderItem;
import com.project.ordermanagementsystem.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Mapper {

    public Product mapToProductEntity(ProductInsertDTO dto){
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setQuantity(dto.getQuantity());

        return product;
    }

    public ProductReadOnlyDTO mapToProductReadOnlyDTO(Product product){
        ProductReadOnlyDTO dto = new ProductReadOnlyDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setQuantity(product.getQuantity());

        return dto;
    }

    public Customer mapToCustomerEntity(CustomerInsertDTO dto){
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber1(dto.getPhoneNumber1());
        customer.setPhoneNumber2(dto.getPhoneNumber2());

        return customer;
    }

    public CustomerReadOnlyDTO mapToCustomerReadOnlyDTO(Customer customer){
        CustomerReadOnlyDTO dto = new CustomerReadOnlyDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber1(customer.getPhoneNumber1());
        dto.setPhoneNumber2(customer.getPhoneNumber2());

        return dto;
    }

    public OrderReadOnlyDTO mapToOrderReadOnlyDTO(Order order){
        OrderReadOnlyDTO dto = new OrderReadOnlyDTO();
        dto.setId(order.getId());
        dto.setCustomer(mapToCustomerReadOnlyDTO(order.getCustomer()));
        dto.setItems(mapToOrderItemListReadOnlyDTO(order.getItems()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dto.setDate(order.getDate().format(formatter));
        dto.setAddress(order.getAddress());

        return dto;
    }

    private List<OrderItemReadOnlyDTO> mapToOrderItemListReadOnlyDTO(List<OrderItem> items){
        if (items == null) return new ArrayList<>();
        return items.stream().map(this::mapToOrderItemReadOnlyDTO).toList();
    }

    private OrderItemReadOnlyDTO mapToOrderItemReadOnlyDTO(OrderItem orderItem){
        OrderItemReadOnlyDTO dto = new OrderItemReadOnlyDTO();
        dto.setId(orderItem.getId());
        dto.setProduct(mapToProductReadOnlyDTO(orderItem.getProduct()));
        dto.setQuantity(orderItem.getQuantity());

        return dto;
    }

}
