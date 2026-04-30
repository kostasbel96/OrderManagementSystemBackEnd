package com.project.ordermanagementsystem.repository;

import com.project.ordermanagementsystem.model.DriverPerson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<DriverPerson, Long>, PersonRepository {

}
