package com.project.ordermanagementsystem.repository;

import com.project.ordermanagementsystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    boolean existsByVatNumberAndActiveTrue(String vatNumber);

}
