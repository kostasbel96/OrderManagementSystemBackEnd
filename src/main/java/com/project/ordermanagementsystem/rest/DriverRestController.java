package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.service.DriverService;
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

    @PostMapping("/drivers/search")
    public ResponseEntity<Page<DriverReadOnlyDTO>> searchDrivers(@RequestBody SearchRequest request){
        Page<DriverReadOnlyDTO> responseDto= driverService.searchDrivers(request);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/drivers/{id}")
    public ResponseEntity<ResponseDTO> getdriverById(@PathVariable Long id){
        ResponseDTO response = driverService.getDriverById(id);
        if(response.getErrorResponse() != null){
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/drivers/update")
    public ResponseEntity<ResponseDTO> updateDriver(@Valid @RequestBody DriverUpdateDTO dto,
                                                      BindingResult bindingResult) {
        ResponseDTO responseDTO;
        responseDTO = driverService.updateDriver(dto, bindingResult);

        if (responseDTO.getErrorResponse() != null){
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/drivers/delete")
    public ResponseEntity<ResponseDTO> deleteDriver(@RequestBody DriverUpdateDTO dto) {
        ResponseDTO responseDTO;
        responseDTO = driverService.deleteDriver(dto);
        if (responseDTO.getErrorResponse() != null) {
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
