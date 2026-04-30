package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.DriverSpecification;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.DriverPerson;
import com.project.ordermanagementsystem.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
                        "DriverPhoneNumber1",
                        "Driver with phone number " + dto.getPhoneNumber1() + " already exists."
                );
            }

            DriverPerson driver = mapper.mapToDriverEntity(dto);
            DriverPerson savedDriver = driverRepository.save(driver);

            LOGGER.info("Driver with id: {} saved successfully.", savedDriver.getId());
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

    public Page<DriverReadOnlyDTO> searchDrivers(SearchRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(
                        Sort.Direction.fromString(request.getSort().getSort()),
                        request.getSort().getField()
                )
        );

        Specification<DriverPerson> spec = Specification.where(DriverSpecification.isActive());

        if (request.getGlobalSearch() != null && !request.getGlobalSearch().isBlank()) {
            spec = spec.and(DriverSpecification.globalSearch(request.getGlobalSearch()));
        }

        if (request.getFilters() != null) {
            for (FilterRequest filter : request.getFilters()) {
                spec = spec.and(DriverSpecification.fromFilter(filter));
            }
        }

        Page<DriverPerson> driverPage = driverRepository.findAll(spec, pageable);

        return driverPage.map(mapper::mapToDriverReadOnlyDTO);
    }

    public ResponseDTO getDriverById(Long id) {
        ResponseDTO response = new ResponseDTO();
        DriverPerson driver;
        try{
            driver = driverRepository.findById(id)
                    .orElseThrow(()-> new AppObjectNotFound("DriverNotFound",String.format("Driver with id: %s not found", id)));
            response.setDriverReadOnlyDTO(mapper.mapToDriverReadOnlyDTO(driver));
            LOGGER.info("Driver with id: {} found successfully.", driver.getId());
        } catch(AppObjectNotFound e){
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =  new ErrorResponse(e.getMessage());
            response.setErrorResponse(errorResponse);
        }
        return response;

    }

    public ResponseDTO updateDriver(DriverUpdateDTO dto, BindingResult bindingResult) {
        ResponseDTO responseDTO = new ResponseDTO();
        DriverPerson existingDriver;
        try {
            existingDriver = driverRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("DriverNotFound", String.format("Driver with id: %s not found.", dto.getId())));

            existingDriver.setId(dto.getId());
            existingDriver.setName(dto.getName());
            existingDriver.setLastName(dto.getLastName());
            existingDriver.setPhoneNumber1(dto.getPhoneNumber1());
            existingDriver.setPhoneNumber2(dto.getPhoneNumber2());

            if (bindingResult.hasErrors()){
                throw new ValidationException(bindingResult);
            }

            DriverPerson updatedDriver = driverRepository.save(existingDriver);
            responseDTO.setDriverReadOnlyDTO(mapper.mapToDriverReadOnlyDTO(updatedDriver));
            LOGGER.info("Driver with id: {} updated successfully.", updatedDriver.getId());
        } catch(AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        } catch (ValidationException e) {
            LOGGER.error(e.getMessage());
            responseDTO.setErrorResponse(new ErrorResponse(e.getBindingResult().getFieldError().getDefaultMessage()));
        }

        return responseDTO;

    }

    @Transactional
    public ResponseDTO deleteDriver(DriverUpdateDTO dto) {
        ResponseDTO responseDTO = new ResponseDTO();
        DriverPerson driverToDelete;
        try {
            driverToDelete = driverRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("DriverNotFound",
                            String.format("Driver with id: %s not found.", dto.getId())));
            if (!driverToDelete.getRoutes().isEmpty()) {
                driverToDelete.setActive(false);
                driverRepository.save(driverToDelete);
            } else {
                driverRepository.delete(driverToDelete);
            }
            DriverReadOnlyDTO returnedDriver = mapper.mapToDriverReadOnlyDTO(driverToDelete);
            responseDTO.setDriverReadOnlyDTO(returnedDriver);
            LOGGER.info("Driver with id: {} deleted successfully.", returnedDriver.getId());
        } catch (AppObjectNotFound e){
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
            LOGGER.error(e.getMessage());
        }

        return responseDTO;

    }
}
