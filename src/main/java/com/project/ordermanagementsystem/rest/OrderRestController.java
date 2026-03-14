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
    public ResponseEntity<Page<OrderReadOnlyDTO>> searchOrdersByCustomerName(@RequestParam(required = false) String name,
                                                                  @RequestParam(required = false) String lastName,
                                                                  @RequestParam(required = false, defaultValue = "date") String sortBy,
                                                                  @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
                                                                  @RequestParam(required = false, defaultValue = "0") int page,
                                                                  @RequestParam(required = false, defaultValue = "5") int pageSize){

        Page<OrderReadOnlyDTO> responseDto =
                orderService.searchOrdersByCustomerName(name, lastName, sortBy, sortDirection, page, pageSize);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ResponseDTO> getOrderById(@PathVariable Long id){
        ResponseDTO responseDto = orderService.getOrderById(id);
        if (responseDto.getErrorResponse() != null){
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/orders/update")
    public ResponseEntity<ResponseDTO> updateOrder(@Valid @RequestBody OrderUpdateDTO dto,
                                                   BindingResult bindingResult) {
        ResponseDTO responseDTO = orderService.updateOrder(dto, bindingResult);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/orders/delete")
    public ResponseEntity<ResponseDTO> deleteOrder(@RequestBody OrderUpdateDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = orderService.deleteOrder(dto);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
