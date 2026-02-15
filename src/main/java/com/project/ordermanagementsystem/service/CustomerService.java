package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.Specifications.CustomerSpecification;
import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.dto.ErrorResponse;
import com.project.ordermanagementsystem.dto.ResponseDTO;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.dto.CustomerInsertDTO;
import com.project.ordermanagementsystem.dto.CustomerReadOnlyDTO;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.repository.CustomerRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private final Mapper mapper;
    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerReadOnlyDTO saveCustomer(CustomerInsertDTO dto) throws AppObjectAlreadyExists {
        if (customerRepository.findCustomerByPhoneNumber1(dto.getPhoneNumber1()).isPresent()){
            LOGGER.error("Customer with phone number: {} already exists.", dto.getPhoneNumber1());
            throw new AppObjectAlreadyExists("CustomerPhoneNumber1", "Customer with phone number " + dto.getPhoneNumber1() + " already exists.");
        }

        Customer customer = mapper.mapToCustomerEntity(dto);
        Customer savedCustomer = customerRepository.save(customer);

        LOGGER.info("Customer with id: {} saved successfully.", savedCustomer.getId());

        return mapper.mapToCustomerReadOnlyDTO(savedCustomer);

    }

    @Transactional
    public Page<CustomerReadOnlyDTO> getPaginatedCustomers(int page, int size){
        String defaultSort = "id";

        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());

        return customerRepository.findAll(pageable).map(mapper::mapToCustomerReadOnlyDTO);
    }

    public List<CustomerReadOnlyDTO> searchCustomers(String name, String lastName){

        Specification<Customer> spec = Specification.where(
                CustomerSpecification.trStringFieldLike("name", name)
        ).or(
                CustomerSpecification.trStringFieldLike("lastName", lastName)
        );

        List<Customer> customers = customerRepository.findAll(spec);

        return customers.stream().map(mapper::mapToCustomerReadOnlyDTO).toList();

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

}
