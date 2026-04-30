package com.project.ordermanagementsystem.repository;

import com.project.ordermanagementsystem.model.DriverPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface DriverRepository extends JpaRepository<DriverPerson, Long>, JpaSpecificationExecutor<DriverPerson>,
        PersonRepository { }
