package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.dto.CustomerInsertDTO;
import com.project.ordermanagementsystem.dto.ErrorResponse;
import com.project.ordermanagementsystem.dto.ResponseDTO;
import com.project.ordermanagementsystem.dto.SupplierInsertDTO;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Supplier;
import com.project.ordermanagementsystem.repository.CustomerRepository;
import com.project.ordermanagementsystem.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupplierService.class);
    private final Mapper mapper;
    private final SupplierRepository supplierRepository;

    @Transactional
    public ResponseDTO saveSupplier(SupplierInsertDTO dto, BindingResult bindingResult) {
        ResponseDTO responseDTO = new ResponseDTO();
        ErrorResponse errorResponse;

        try{
            if(bindingResult.hasErrors()){
                throw new ValidationException(bindingResult);
            }

            if (supplierRepository.existsByVatNumberAndActiveTrue(dto.getVat())) {
                throw new AppObjectAlreadyExists(
                        "SupplierVatNumber",
                        "Supplier with vat number " + dto.getVat() + " already exists."
                );
            }

            Supplier supplier = mapper.mapToSupplierEntity(dto);
            Supplier savedSupplier = supplierRepository.save(supplier);

            LOGGER.info("Supplier with id: {} saved successfully.", savedSupplier.getId());
            responseDTO.setSupplierReadOnlyDTO(mapper.mapToSupplierReadOnlyDTO(savedSupplier));
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
