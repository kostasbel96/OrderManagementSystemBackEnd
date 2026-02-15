package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
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
    public ResponseEntity<ProductReadOnlyDTO> saveProduct(
            @Valid @RequestBody ProductInsertDTO productInsertDTO,
            BindingResult bindingResult) throws AppObjectAlreadyExists, ValidationException {

        if(bindingResult.hasErrors()){
            throw new ValidationException(bindingResult);
        }

        ProductReadOnlyDTO productReadOnlyDTO = productService.saveProduct(productInsertDTO);
        return new ResponseEntity<>(productReadOnlyDTO, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductReadOnlyDTO>> getPaginatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size){
        Page<ProductReadOnlyDTO> productsPage = productService.getPaginatedProducts(page, size);
        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<ProductReadOnlyDTO>> searchProducts(@RequestParam(required = false) String name){
        List<ProductReadOnlyDTO> responseDto= productService.searchProducts(name);
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

}
