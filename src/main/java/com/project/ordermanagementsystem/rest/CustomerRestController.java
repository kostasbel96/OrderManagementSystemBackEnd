package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.service.CustomerService;
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
public class CustomerRestController {

    private final CustomerService customerService;

    @PostMapping("customers/save")
    public ResponseEntity<ResponseDTO> saveCustomer(
            @Valid @RequestBody CustomerInsertDTO customerInsertDTO,
            BindingResult bindingResult) {
        ResponseDTO responseDTO = customerService.saveCustomer(customerInsertDTO, bindingResult);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @GetMapping("/customers")
    public ResponseEntity<Page<CustomerReadOnlyDTO>> getPaginatedCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection){
        Page<CustomerReadOnlyDTO> customersPage = customerService.getPaginatedCustomers(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(customersPage, HttpStatus.OK);
    }

    @PostMapping("/customers/search")
    public ResponseEntity<Page<CustomerReadOnlyDTO>> searchCustomers(@RequestBody SearchRequest request){
        Page<CustomerReadOnlyDTO> responseDto= customerService.searchCustomers(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }


    @GetMapping("/customers/{id}")
    public ResponseEntity<ResponseDTO> getCustomerById(@PathVariable Long id){
        ResponseDTO response = customerService.getCustomerById(id);
        if(response.getErrorResponse() != null){
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/customers/update")
    public ResponseEntity<ResponseDTO> updateCustomer(@Valid @RequestBody CustomerUpdateDTO dto,
                                                      BindingResult bindingResult) {
        ResponseDTO responseDTO;
        responseDTO = customerService.updateCustomer(dto, bindingResult);

        if (responseDTO.getErrorResponse() != null){
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/customers/delete")
    public ResponseEntity<ResponseDTO> deleteCustomer(@RequestBody CustomerUpdateDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = customerService.deleteCustomer(dto);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
