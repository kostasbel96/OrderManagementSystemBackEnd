package com.project.ordermanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {
    OrderReadOnlyDTO orderReadOnlyDTO;
    ErrorResponse errorResponse;
    CustomerReadOnlyDTO customerReadOnlyDTO;
}
