package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.ResponseDTO;
import com.project.ordermanagementsystem.dto.SupplierInsertDTO;
import com.project.ordermanagementsystem.service.SupplierService;
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
public class SupplierRestController {

    private final SupplierService supplierService;

    @PostMapping("suppliers/save")
    public ResponseEntity<ResponseDTO> saveSupplier(@Valid @RequestBody SupplierInsertDTO supplierInsertDTO,
                                                    BindingResult bindingResult) {
        ResponseDTO responseDTO = supplierService.saveSupplier(supplierInsertDTO, bindingResult);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


}
