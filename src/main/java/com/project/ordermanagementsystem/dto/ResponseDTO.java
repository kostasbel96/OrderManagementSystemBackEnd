package com.project.ordermanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ordermanagementsystem.model.Receipt;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {
    @JsonProperty("orderItems")
    List<OrderReadOnlyDTO> orderItems;
    @JsonProperty("orderItem")
    OrderReadOnlyDTO orderReadOnlyDTO;
    @JsonProperty("errorResponse")
    ErrorResponse errorResponse;
    @JsonProperty("customer")
    CustomerReadOnlyDTO customerReadOnlyDTO;
    @JsonProperty("driver")
    DriverReadOnlyDTO driverReadOnlyDTO;
    @JsonProperty("productDto")
    ProductReadOnlyDTO productReadOnlyDTO;
    @JsonProperty("route")
    RouteReadOnlyDTO routeReadOnlyDTO;
    @JsonProperty("receipt")
    ReceiptReadOnlyDTO receiptReadOnlyDTO;
    @JsonProperty("supplier")
    SupplierReadOnlyDTO supplierReadOnlyDTO;
}
