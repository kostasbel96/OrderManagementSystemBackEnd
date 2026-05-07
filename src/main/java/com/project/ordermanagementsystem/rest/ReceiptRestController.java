package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.ReceiptInsertDTO;
import com.project.ordermanagementsystem.dto.ResponseDTO;
import com.project.ordermanagementsystem.dto.RouteInsertDTO;
import com.project.ordermanagementsystem.model.Receipt;
import com.project.ordermanagementsystem.service.ReceiptService;
import com.project.ordermanagementsystem.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class ReceiptRestController {


    private final ReceiptService receiptService;

    @PostMapping("receipts/save")
    public ResponseEntity<ResponseDTO> saveReceipt(
            @Valid @RequestBody ReceiptInsertDTO receiptInsertDTO,
            BindingResult bindingResult) {

        ResponseDTO responseDto = receiptService.saveReceipt(receiptInsertDTO, bindingResult);

        if (responseDto.getErrorResponse() != null){
            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);

    }

}
