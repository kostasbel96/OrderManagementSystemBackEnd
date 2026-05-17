package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.dto.OrderReadOnlyDTO;
import com.project.ordermanagementsystem.dto.PurchaseOrderInsertDTO;
import com.project.ordermanagementsystem.dto.PurchaseOrderReadOnlyDTO;
import com.project.ordermanagementsystem.dto.SearchRequest;
import com.project.ordermanagementsystem.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PurchaseOrderRestController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("purchaseOrders/save")
    public ResponseEntity<PurchaseOrderReadOnlyDTO> saveOrder(
            @Valid @RequestBody PurchaseOrderInsertDTO purchaseOrderInsertDTO,
            BindingResult bindingResult) throws ValidationException,
            AppObjectNotFound {

        if(bindingResult.hasErrors()){
            throw new ValidationException(bindingResult);
        }

        PurchaseOrderReadOnlyDTO orderReadOnlyDTO =
                purchaseOrderService.savePurchaseOrder(purchaseOrderInsertDTO);

        return new ResponseEntity<>(orderReadOnlyDTO, HttpStatus.OK);
    }

    @PostMapping("purchaseOrders/search")
    public ResponseEntity<Page<PurchaseOrderReadOnlyDTO>> searchPurchaseOrders(@RequestBody SearchRequest request){

        Page<PurchaseOrderReadOnlyDTO> responseDto = purchaseOrderService.searchPurchaseOrders(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

}
