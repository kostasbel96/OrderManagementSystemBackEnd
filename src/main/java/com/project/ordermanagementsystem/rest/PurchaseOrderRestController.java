package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

        if(bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        PurchaseOrderReadOnlyDTO orderReadOnlyDTO =
                purchaseOrderService.savePurchaseOrder(purchaseOrderInsertDTO);

        return new ResponseEntity<>(orderReadOnlyDTO, HttpStatus.CREATED);
    }

    @PostMapping("purchaseOrders/search")
    public ResponseEntity<Page<PurchaseOrderReadOnlyDTO>> searchPurchaseOrders(@RequestBody SearchRequest request){

        Page<PurchaseOrderReadOnlyDTO> responseDto = purchaseOrderService.searchPurchaseOrders(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/purchaseOrders/{id}")
    public ResponseEntity<ResponseDTO> getOrderById(@PathVariable Long id){
        ResponseDTO responseDto = purchaseOrderService.getPurchaseOrderById(id);
        if (responseDto.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/purchaseOrders/update")
    public ResponseEntity<ResponseDTO> updatePurchaseOrder(@Valid @RequestBody PurchaseOrderUpdateDTO dto,
                                                   BindingResult bindingResult) {
        ResponseDTO responseDTO = purchaseOrderService.updatePurchaseOrder(dto, bindingResult);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/purchaseOrders/delete")
    public ResponseEntity<ResponseDTO> deleteOrder(@RequestBody PurchaseOrderUpdateDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = purchaseOrderService.deletePurchaseOrder(dto);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
