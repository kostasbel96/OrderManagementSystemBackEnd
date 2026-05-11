package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.service.SupplierService;
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

    @PostMapping("/suppliers/search")
    public ResponseEntity<Page<SupplierReadOnlyDTO>> searchSuppliers(@RequestBody SearchRequest request){
        Page<SupplierReadOnlyDTO> responseDto= supplierService.searchSuppliers(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/suppliers/update")
    public ResponseEntity<ResponseDTO> updateSupplier(@Valid @RequestBody SupplierUpdateDTO dto,
                                                      BindingResult bindingResult) {
        ResponseDTO responseDTO;
        responseDTO = supplierService.updateSupplier(dto, bindingResult);

        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/suppliers/delete")
    public ResponseEntity<ResponseDTO> deleteSupplier(@RequestBody SupplierUpdateDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = supplierService.deleteSupplier(dto);

        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
