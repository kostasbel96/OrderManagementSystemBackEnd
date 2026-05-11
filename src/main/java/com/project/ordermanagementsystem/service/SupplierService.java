package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.CustomerSpecification;
import com.project.ordermanagementsystem.core.specifications.SupplierSpecification;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Supplier;
import com.project.ordermanagementsystem.repository.CustomerRepository;
import com.project.ordermanagementsystem.repository.SupplierRepository;
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

    public Page<SupplierReadOnlyDTO> searchSuppliers(SearchRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(
                        Sort.Direction.fromString(request.getSort().getSort()),
                        request.getSort().getField()
                )
        );

        Specification<Supplier> spec = Specification.where(SupplierSpecification.isActive());

        if (request.getGlobalSearch() != null && !request.getGlobalSearch().isBlank()) {
            spec = spec.and(SupplierSpecification.globalSearch(request.getGlobalSearch()));
        }

        if (request.getFilters() != null) {
            for (FilterRequest filter : request.getFilters()) {
                spec = spec.and(SupplierSpecification.fromFilter(filter));
            }
        }

        Page<Supplier> supplierPage = supplierRepository.findAll(spec, pageable);

        return supplierPage.map(mapper::mapToSupplierReadOnlyDTO);
    }

    @Transactional
    public ResponseDTO updateSupplier(SupplierUpdateDTO dto, BindingResult bindingResult) {
        ResponseDTO responseDTO = new ResponseDTO();
        Supplier existingSupplier;
        try {
            existingSupplier = supplierRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "SupplierNotFound",
                            String.format("Supplier with id: %s not found.", dto.getId())
                    ));

            existingSupplier.setId(dto.getId());
            existingSupplier.setName(dto.getName());
            existingSupplier.setEmail(dto.getEmail());
            existingSupplier.setVatNumber(dto.getVat());
            existingSupplier.setAddress(dto.getAddress());
            existingSupplier.setPhoneNumber1(dto.getPhoneNumber1());
            existingSupplier.setPhoneNumber2(dto.getPhoneNumber2());

            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            Supplier updatedSupplier = supplierRepository.save(existingSupplier);
            responseDTO.setSupplierReadOnlyDTO(mapper.mapToSupplierReadOnlyDTO(updatedSupplier));
            LOGGER.info("Supplier with id: {} updated successfully.", updatedSupplier.getId());

        } catch (AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        } catch (ValidationException e) {
            LOGGER.error(e.getMessage());
            responseDTO.setErrorResponse(new ErrorResponse(e.getBindingResult().getFieldError().getDefaultMessage()));
        }

        return responseDTO;
    }

    @Transactional
    public ResponseDTO deleteSupplier(SupplierUpdateDTO dto) {
        ResponseDTO responseDTO = new ResponseDTO();
        Supplier supplierToDelete;
        try {
            supplierToDelete = supplierRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("SupplierNotFound",
                            String.format("Supplier with id: %s not found.", dto.getId())));

            if (!supplierToDelete.getPurchaseOrders().isEmpty()) {
                supplierToDelete.setActive(false);
                supplierRepository.save(supplierToDelete);
            } else {
                supplierRepository.delete(supplierToDelete);
            }

            SupplierReadOnlyDTO returnedSupplier = mapper.mapToSupplierReadOnlyDTO(supplierToDelete);
            responseDTO.setSupplierReadOnlyDTO(returnedSupplier);
            LOGGER.info("Supplier with id: {} deleted successfully.", returnedSupplier.getId());

        } catch (AppObjectNotFound e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
            LOGGER.error(e.getMessage());
        }

        return responseDTO;
    }
}
