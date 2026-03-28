package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.service.ProductService;
import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    @PostMapping("products/save")
    public ResponseEntity<ResponseDTO> saveProduct(
            @Valid @RequestBody ProductInsertDTO productInsertDTO,
            BindingResult bindingResult){

        ResponseDTO responseDTO = new ResponseDTO();
        if(bindingResult.hasErrors()){
            responseDTO.setErrorResponse(new ErrorResponse(bindingResult.getFieldErrors()
                    .getFirst()
                    .getDefaultMessage()));
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        responseDTO = productService.saveProduct(productInsertDTO);
        if (responseDTO.getErrorResponse() != null){
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductReadOnlyDTO>> getPaginatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection){
        Page<ProductReadOnlyDTO> productsPage = productService.getPaginatedProducts(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }

    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductReadOnlyDTO>> searchProducts(@RequestParam(required = false) String name,
                                                                   @RequestParam(required = false, defaultValue = "name") String sortBy,
                                                                   @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
                                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                                   @RequestParam(required = false, defaultValue = "5") int pageSize){
        Page<ProductReadOnlyDTO> responseDto = productService.searchProducts(name, sortBy, sortDirection, page, pageSize);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/products/{name}")
    public ResponseEntity<ResponseDTO> getProductByName(@PathVariable String name) {
        ResponseDTO responseDto;
        responseDto = productService.getProductByName(name);
        if(responseDto.getErrorResponse() != null){
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/products/update")
    public ResponseEntity<ResponseDTO> updateProduct(@Valid @RequestBody ProductUpdateDTO dto,
                                                     BindingResult bindingResult) {
        ResponseDTO responseDTO;
        responseDTO = productService.updateProduct(dto, bindingResult);

        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/products/delete")
    public ResponseEntity<ResponseDTO> deleteProduct(@RequestBody ProductUpdateDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = productService.deleteProduct(dto);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
