package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.dto.DriverInsertDTO;
import com.project.ordermanagementsystem.dto.ErrorResponse;
import com.project.ordermanagementsystem.dto.ResponseDTO;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.DriverPerson;
import com.project.ordermanagementsystem.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class DriverService {


    private static final Logger LOGGER = LoggerFactory.getLogger(DriverService.class);
    private final Mapper mapper;
    private final DriverRepository driverRepository;

    @Transactional
    public ResponseDTO saveDriver(DriverInsertDTO dto, BindingResult bindingResult) {
        ResponseDTO responseDTO = new ResponseDTO();
        ErrorResponse errorResponse;

        try{
            if(bindingResult.hasErrors()){
                throw new ValidationException(bindingResult);
            }

            if (driverRepository.existsByPhoneNumber1AndActiveTrue(dto.getPhoneNumber1())) {
                throw new AppObjectAlreadyExists(
                        "CustomerPhoneNumber1",
                        "Customer with phone number " + dto.getPhoneNumber1() + " already exists."
                );
            }

            DriverPerson driver = mapper.mapToDriverEntity(dto);
            DriverPerson savedDriver = driverRepository.save(driver);

            LOGGER.info("Customer with id: {} saved successfully.", savedDriver.getId());
            responseDTO.setDriverReadOnlyDTO(mapper.mapToDriverReadOnlyDTO(savedDriver));
        } catch (AppObjectAlreadyExists e) {
            LOGGER.error(e.getMessage());
            errorResponse = new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        } catch (ValidationException e) {
            LOGGER.error(e.getMessage());
            errorResponse = new ErrorResponse(e.getBindingResult().getFieldError().getDefaultMessage());
            responseDTO.setErrorResponse(errorResponse);
        }
        return responseDTO;
    }
}
