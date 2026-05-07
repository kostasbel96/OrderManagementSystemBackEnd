package com.project.ordermanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReceiptInsertDTO {

    @NotNull
    private String amount;

    private String notes;

    @NotNull
    private Long customerId;

    private List<Long> orderIds;


}
