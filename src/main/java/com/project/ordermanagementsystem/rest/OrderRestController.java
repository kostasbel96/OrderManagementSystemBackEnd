package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.core.exceptions.AppObjectInvalidQuantity;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.dto.OrderInsertDTO;
import com.project.ordermanagementsystem.dto.OrderReadOnlyDTO;
import com.project.ordermanagementsystem.service.OrderService;
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
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping("orders/save")
    public ResponseEntity<OrderReadOnlyDTO> saveOrder(
            @Valid @RequestBody OrderInsertDTO orderInsertDTO,
            BindingResult bindingResult) throws ValidationException,
            AppObjectNotFound, AppObjectInvalidQuantity {

        if(bindingResult.hasErrors()){
            throw new ValidationException(bindingResult);
        }

        OrderReadOnlyDTO orderReadOnlyDTO = orderService.saveOrder(orderInsertDTO);
        return new ResponseEntity<>(orderReadOnlyDTO, HttpStatus.OK);
    }

}
