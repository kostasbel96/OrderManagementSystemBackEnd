package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.CustomerSpecification;
import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.repository.CustomerRepository;
import com.project.ordermanagementsystem.repository.ProductRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private final Mapper mapper;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ResponseDTO saveCustomer(CustomerInsertDTO dto, BindingResult bindingResult) {
        ResponseDTO responseDTO = new ResponseDTO();
        ErrorResponse errorResponse;

        try{
            if(bindingResult.hasErrors()){
                throw new ValidationException(bindingResult);
            }

            if (customerRepository.existsByPhoneNumber1AndActiveTrue(dto.getPhoneNumber1())) {
                throw new AppObjectAlreadyExists(
                        "CustomerPhoneNumber1",
                        "Customer with phone number " + dto.getPhoneNumber1() + " already exists."
                );
            }

            Customer customer = mapper.mapToCustomerEntity(dto);
            Customer savedCustomer = customerRepository.save(customer);

            LOGGER.info("Customer with id: {} saved successfully.", savedCustomer.getId());
             responseDTO.setCustomerReadOnlyDTO(mapper.mapToCustomerReadOnlyDTO(savedCustomer));
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

    @Transactional
    public Page<CustomerReadOnlyDTO> getPaginatedCustomers(int page, int size, String sortBy, String sortDirection){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Specification<Customer> spec = Specification.where(CustomerSpecification.isActive());
        return customerRepository.findAll(spec, pageable).map(mapper::mapToCustomerReadOnlyDTO);
    }

    public Page<CustomerReadOnlyDTO> searchCustomers(String name, String lastName, String sortBy, String sortDirection, int page, int pageSize){

        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Specification<Customer> spec = Specification.where(
                CustomerSpecification.trStringFieldLike("name", name)
        ).or(
                CustomerSpecification.trStringFieldLike("lastName", lastName)
        ).and(
                CustomerSpecification.isActive()
        );



        Page<Customer> customersPage = customerRepository.findAll(spec, pageable);

        return customersPage.map(mapper::mapToCustomerReadOnlyDTO);

    }

    public ResponseDTO getCustomerById(Long id) {
        ResponseDTO response = new ResponseDTO();
        Customer customer;
        try{
            customer = customerRepository.findById(id)
                    .orElseThrow(()-> new AppObjectNotFound("CustomerNotFound",String.format("Customer with id: %s not found", id)));
            response.setCustomerReadOnlyDTO(mapper.mapToCustomerReadOnlyDTO(customer));
            LOGGER.info("Customer with id: {} found successfully.", customer.getId());
        } catch(AppObjectNotFound e){
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =  new ErrorResponse(e.getMessage());
            response.setErrorResponse(errorResponse);
        }
        return response;

    }

    public ResponseDTO updateCustomer(CustomerUpdateDTO dto, BindingResult bindingResult) {
        ResponseDTO responseDTO = new ResponseDTO();
        Customer existingCustomer;
        try {
            existingCustomer = customerRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("CustomerNotFound", String.format("Customer with id: %s not found.", dto.getId())));

            existingCustomer.setId(dto.getId());
            existingCustomer.setName(dto.getName());
            existingCustomer.setLastName(dto.getLastName());
            existingCustomer.setPhoneNumber1(dto.getPhoneNumber1());
            existingCustomer.setPhoneNumber2(dto.getPhoneNumber2());
            existingCustomer.setEmail(dto.getEmail());

            if (bindingResult.hasErrors()){
                throw new ValidationException(bindingResult);
            }

            Customer updatedCustomer = customerRepository.save(existingCustomer);
            responseDTO.setCustomerReadOnlyDTO(mapper.mapToCustomerReadOnlyDTO(updatedCustomer));
            LOGGER.info("Customer with id: {} updated successfully.", updatedCustomer.getId());
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
    public ResponseDTO deleteCustomer(CustomerUpdateDTO dto) {
        ResponseDTO responseDTO = new ResponseDTO();
        Customer customerToDelete;
        try {
            customerToDelete = customerRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("CustomerNotFound",
                            String.format("Customer with id: %s not found.", dto.getId())));
        if (!customerToDelete.getOrders().isEmpty()) {
            customerToDelete.setActive(false);
            customerRepository.save(customerToDelete);
        } else {
            customerRepository.delete(customerToDelete);
        }
        CustomerReadOnlyDTO returnedCustomer = mapper.mapToCustomerReadOnlyDTO(customerToDelete);
        responseDTO.setCustomerReadOnlyDTO(returnedCustomer);
        LOGGER.info("Customer with id: {} deleted successfully.", returnedCustomer.getId());
        } catch (AppObjectNotFound e){
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
            LOGGER.error(e.getMessage());
        }

        return responseDTO;

    }

}
