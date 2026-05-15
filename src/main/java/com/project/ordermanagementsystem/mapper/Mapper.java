package com.project.ordermanagementsystem.mapper;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class Mapper {

    public Product mapToProductEntity(ProductInsertDTO dto){
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setQuantity(dto.getQuantity());
        product.setPrice(new BigDecimal(dto.getPrice()));

        return product;
    }

    public ProductReadOnlyDTO mapToProductReadOnlyDTO(Product product){
        ProductReadOnlyDTO dto = new ProductReadOnlyDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setQuantity(product.getQuantity());
        dto.setPrice(product.getPrice().toString());

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

    public PurchaseOrderReadOnlyDTO mapToPurchaseOrderReadOnlyDTO(PurchaseOrder purchaseOrder) {
        PurchaseOrderReadOnlyDTO dto = new PurchaseOrderReadOnlyDTO();
        dto.setId(purchaseOrder.getId());
        dto.setSupplier(mapToSupplierReadOnlyDTO(purchaseOrder.getSupplier()));
        dto.setItems(purchaseOrder.getItems().stream().map(this::mapToPurchaseOrderItemReadOnlyDTO).toList());
        dto.setStatus(purchaseOrder.getStatus());
        dto.setTotal(purchaseOrder.getTotal().toString());
        dto.setPaymentStatus(purchaseOrder.getPaymentStatus());
        dto.setPaidAmount(purchaseOrder.getPaidAmount().toString());

        return dto;
    }

    public PurchaseOrderItemReadOnlyDTO mapToPurchaseOrderItemReadOnlyDTO(PurchaseOrderItem purchaseOrderItem) {
        PurchaseOrderItemReadOnlyDTO purchaseOrderItemReadOnlyDTO = new PurchaseOrderItemReadOnlyDTO();
        purchaseOrderItemReadOnlyDTO.setId(purchaseOrderItem.getId());
        purchaseOrderItemReadOnlyDTO.setProduct(mapToProductReadOnlyDTO(purchaseOrderItem.getProduct()));
        purchaseOrderItemReadOnlyDTO.setQuantity(purchaseOrderItem.getQuantity());
        BigDecimal price = purchaseOrderItem.getPrice();
        purchaseOrderItemReadOnlyDTO.setPrice(price != null ? price.toString() : null);

        return purchaseOrderItemReadOnlyDTO;
    }

    public CustomerReadOnlyDTO mapToCustomerReadOnlyDTO(Customer customer){
        CustomerReadOnlyDTO dto = new CustomerReadOnlyDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber1(customer.getPhoneNumber1());
        dto.setPhoneNumber2(customer.getPhoneNumber2());
        dto.setBalance(customer.getBalance() != null ? customer.getBalance().toString() : "0.0");

        return dto;
    }

    public Supplier mapToSupplierEntity(SupplierInsertDTO dto){
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setAddress(dto.getAddress());
        supplier.setPhoneNumber1(dto.getPhoneNumber1());
        supplier.setPhoneNumber2(dto.getPhoneNumber2());
        supplier.setEmail(dto.getEmail());
        supplier.setVatNumber(dto.getVat());

        return supplier;
    }

    public DriverReadOnlyDTO mapToDriverReadOnlyDTO(DriverPerson driver){
        DriverReadOnlyDTO dto = new DriverReadOnlyDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setLastName(driver.getLastName());
        dto.setPhoneNumber1(driver.getPhoneNumber1());
        dto.setPhoneNumber2(driver.getPhoneNumber2());

        return dto;
    }

    public DriverPerson mapToDriverEntity(DriverInsertDTO dto){
        DriverPerson driver = new DriverPerson();
        driver.setName(dto.getName());
        driver.setLastName(dto.getLastName());
        driver.setPhoneNumber1(dto.getPhoneNumber1());
        driver.setPhoneNumber2(dto.getPhoneNumber2());

        return driver;
    }

    public SupplierReadOnlyDTO mapToSupplierReadOnlyDTO(Supplier supplier) {
        SupplierReadOnlyDTO dto = new SupplierReadOnlyDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setAddress(supplier.getAddress());
        dto.setPhoneNumber1(supplier.getPhoneNumber1());
        dto.setPhoneNumber2(supplier.getPhoneNumber2());
        dto.setEmail(supplier.getEmail());
        dto.setVat(supplier.getVatNumber());
        dto.setBalance(supplier.getBalance() != null ? supplier.getBalance().toString() : "0.0");

        return dto;
    }

    public ReceiptReadOnlyDTO mapToReceiptReadOnlyDTO(Receipt receipt) {
        ReceiptReadOnlyDTO dto = new ReceiptReadOnlyDTO();
        dto.setId(receipt.getId());
        dto.setCustomer(mapToCustomerReadOnlyDTO(receipt.getCustomer()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dto.setDate(receipt.getDate().format(formatter));
        dto.setAmount(receipt.getAmount().toString());
        dto.setNotes(receipt.getNotes());

        return dto;
    }

    public OrderReadOnlyDTO mapToOrderReadOnlyDTO(Order order){
        OrderReadOnlyDTO dto = new OrderReadOnlyDTO();
        dto.setId(order.getId());
        dto.setCustomer(mapToCustomerReadOnlyDTO(order.getCustomer()));
        dto.setItems(mapToOrderItemListReadOnlyDTO(order.getItems()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dto.setDate(order.getDate().format(formatter));
        dto.setStatus(order.getStatus());
        dto.setAddress(order.getAddress());
        dto.setPaidAmount(order.getPaidAmount() != null ? order.getPaidAmount().toString() : "");
        order.calculateTotalAmount();
        dto.setTotal(order.getTotal().toString());
        dto.setPaymentStatus(order.getPaymentStatus());

        return dto;
    }

    public RouteReadOnlyDTO mapToRouteReadOnlyDTO(Route route){
        RouteReadOnlyDTO dto = new RouteReadOnlyDTO();
        dto.setId(route.getId());
        dto.setDriver(mapToDriverReadOnlyDTO(route.getDriver()));
        dto.setOrders(
                route.getOrders() == null
                        ? List.of()
                        : route.getOrders().stream()
                        .map(this::mapToOrderReadOnlyDTO)
                        .toList()
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dto.setDate(route.getDate().format(formatter));
        dto.setNotes(route.getNotes());
        dto.setName(route.getName());
        dto.setStatus(route.getStatus());
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
        dto.setPrice(orderItem.getPrice().toString());

        return dto;
    }

}
