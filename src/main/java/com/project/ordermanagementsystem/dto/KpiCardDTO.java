package com.project.ordermanagementsystem.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KpiCardDTO {

    @JsonProperty("productKpi")
    ProductKpiDTO productKpiDTO;
    @JsonProperty("orderKpi")
    OrderKpiDTO orderKpiDTO;
    @JsonProperty("customerKpi")
    CustomerKpiDTO customerKpiDTO;


}
