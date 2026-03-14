package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.model.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    private ProductSpecification(){

    }

    public static Specification<Product> trStringFieldLike(String field, String value){
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));
            return builder.like(builder.upper(root.get(field)), "%" + value.toUpperCase() + "%");
        };
    }

    public static Specification<Product> isActive() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

}
