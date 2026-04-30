package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.DriverInsertDTO;
import com.project.ordermanagementsystem.dto.ResponseDTO;
import com.project.ordermanagementsystem.service.DriverService;
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
public class DriverRestController {

    private final DriverService driverService;

    @PostMapping("drivers/save")
    public ResponseEntity<ResponseDTO> saveDriver(
            @Valid @RequestBody DriverInsertDTO driverInsertDTO,
            BindingResult bindingResult) {
        ResponseDTO responseDTO = driverService.saveDriver(driverInsertDTO, bindingResult);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
