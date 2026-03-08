package com.project.ordermanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    ErrorResponse errorResponse;
    @JsonProperty("customer")
    CustomerReadOnlyDTO customerReadOnlyDTO;
    @JsonProperty("productDto")
    ProductReadOnlyDTO productReadOnlyDTO;
}
