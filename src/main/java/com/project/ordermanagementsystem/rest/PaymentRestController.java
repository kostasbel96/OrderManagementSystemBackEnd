package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.service.PaymentService;
import com.project.ordermanagementsystem.service.ReceiptService;
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
public class PaymentRestController {


    private final PaymentService paymentService;

    @PostMapping("payments/save")
    public ResponseEntity<ResponseDTO> savePayment(
            @Valid @RequestBody PaymentInsertDTO paymentInsertDTO,
            BindingResult bindingResult) {

        ResponseDTO responseDto = paymentService.savePayment(paymentInsertDTO, bindingResult);

        if (responseDto.getErrorResponse() != null){
            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);

    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ResponseDTO> getPaymentById(@PathVariable Long id){
        ResponseDTO responseDto = paymentService.getPaymentById(id);
        if (responseDto.getErrorResponse() != null){
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/payments/search")
    public ResponseEntity<Page<PaymentReadOnlyDTO>> searchPayments(@RequestBody SearchRequest request){

        Page<PaymentReadOnlyDTO> responseDto = paymentService.searchPayments(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/payments/delete")
    public ResponseEntity<ResponseDTO> deletePayment(@RequestBody PaymentDeleteDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = paymentService.deletePayment(dto.getId());
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
