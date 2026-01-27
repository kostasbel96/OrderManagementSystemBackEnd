package com.project.ordermanagementsystem.core.Specifications;

import com.project.ordermanagementsystem.model.Customer;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    private CustomerSpecification(){

    }

    public static Specification<Customer> trStringFieldLike(String field, String value){
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));
            return builder.like(builder.upper(root.get(field)), "%" + value.toUpperCase() + "%");
        };
    }

}
