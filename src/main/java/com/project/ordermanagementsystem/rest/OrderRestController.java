package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.core.exceptions.AppObjectInvalidQuantity;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.service.OrderService;
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

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderReadOnlyDTO>> getPaginatedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size){
        Page<OrderReadOnlyDTO> ordersPage = orderService.getPaginatedOrders(page, size);
        return new ResponseEntity<>(ordersPage, HttpStatus.OK);
    }

    @GetMapping("/orders/search")
    public ResponseEntity<List<OrderReadOnlyDTO>> searchOrdersByCustomerName(@RequestParam(required = false) String name,
                                                                             @RequestParam(required = false) String lastName){

        List<OrderReadOnlyDTO> responseDto = orderService.searchOrdersByCustomerName(name, lastName);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ResponseDTO> getOrderById(@PathVariable Long id){
        OrderReadOnlyDTO order;
        ResponseDTO responseDto = new ResponseDTO();
        try {
            order = orderService.getOrderById(id);
            responseDto.setOrderReadOnlyDTO(order);
        } catch (AppObjectNotFound e) {
            ErrorResponse errorResponse =
                    new ErrorResponse(String.format("Order with id %s: %s", id, e.getMessage()));
            responseDto.setErrorResponse(errorResponse);
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
