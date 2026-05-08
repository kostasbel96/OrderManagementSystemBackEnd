package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.model.Receipt;
import com.project.ordermanagementsystem.service.ReceiptService;
import com.project.ordermanagementsystem.service.RouteService;
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

    @GetMapping("/receipts/{id}")
    public ResponseEntity<ResponseDTO> getReceiptById(@PathVariable Long id){
        ResponseDTO responseDto = receiptService.getReceiptById(id);
        if (responseDto.getErrorResponse() != null){
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/receipts/search")
    public ResponseEntity<Page<ReceiptReadOnlyDTO>> searchReceipts(@RequestBody SearchRequest request){

        Page<ReceiptReadOnlyDTO> responseDto = receiptService.searchReceipts(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/receipts/delete")
    public ResponseEntity<ResponseDTO> deleteOrder(@RequestBody ReceiptDeleteDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = receiptService.deleteReceipt(dto.getId());
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
