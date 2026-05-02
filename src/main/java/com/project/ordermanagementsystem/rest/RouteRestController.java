package com.project.ordermanagementsystem.rest;


import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;

import com.project.ordermanagementsystem.dto.*;
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
public class RouteRestController {

    private final RouteService routeService;

    @PostMapping("routes/save")
    public ResponseEntity<RouteReadOnlyDTO> saveRoute(
            @Valid @RequestBody RouteInsertDTO routeInsertDTO,
            BindingResult bindingResult) throws ValidationException, AppObjectNotFound, AppObjectAlreadyExists {

        if(bindingResult.hasErrors()){
            throw new ValidationException(bindingResult);
        }

        RouteReadOnlyDTO routeReadOnlyDTO = routeService.saveRoute(routeInsertDTO);
        return new ResponseEntity<>(routeReadOnlyDTO, HttpStatus.OK);
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<ResponseDTO> getRouteById(@PathVariable Long id){
        ResponseDTO responseDto = routeService.getRouteById(id);
        if (responseDto.getErrorResponse() != null){
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/routes/search")
    public ResponseEntity<Page<RouteReadOnlyDTO>> searchRoutes(@RequestBody SearchRequest request){

        Page<RouteReadOnlyDTO> responseDto = routeService.searchRoutes(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/routes/update")
    public ResponseEntity<ResponseDTO> updateRoute(@Valid @RequestBody RouteUpdateDTO dto,
                                                   BindingResult bindingResult) {
        ResponseDTO responseDTO = routeService.updateRoute(dto, bindingResult);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/routes/delete")
    public ResponseEntity<ResponseDTO> deleteRoute(@RequestBody RouteUpdateDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = routeService.deleteRoute(dto.getId());
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
