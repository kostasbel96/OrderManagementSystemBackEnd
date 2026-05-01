package com.project.ordermanagementsystem.rest;


import com.project.ordermanagementsystem.core.exceptions.AppObjectInvalidQuantity;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;

import com.project.ordermanagementsystem.dto.RouteInsertDTO;
import com.project.ordermanagementsystem.dto.RouteReadOnlyDTO;
import com.project.ordermanagementsystem.service.RouteService;
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
public class RouteRestController {

    private final RouteService routeService;

    @PostMapping("routes/save")
    public ResponseEntity<RouteReadOnlyDTO> saveRoute(
            @Valid @RequestBody RouteInsertDTO routeInsertDTO,
            BindingResult bindingResult) throws ValidationException, AppObjectNotFound {

        if(bindingResult.hasErrors()){
            throw new ValidationException(bindingResult);
        }

        RouteReadOnlyDTO routeReadOnlyDTO = routeService.saveRoute(routeInsertDTO);
        return new ResponseEntity<>(routeReadOnlyDTO, HttpStatus.OK);
    }

}
