package com.project.ordermanagementsystem.repository;

import com.project.ordermanagementsystem.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findCustomerByPhoneNumber1(String phoneNumber);
    Page<Customer> findByActiveTrue(Pageable pageable);
    boolean existsByPhoneNumber1AndActiveTrue(String phoneNumber1);

}
