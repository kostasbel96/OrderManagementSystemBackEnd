package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Order;
import com.project.ordermanagementsystem.model.Product;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

    private OrderSpecification(){

    }

    public static Specification<Order> trStringFieldLike(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));

            Join<Order, Customer> customerJoin = root.getJoins().stream()
                    .filter(j -> j.getAttribute().getName().equals("customer"))
                    .map(j -> (Join<Order, Customer>) j)
                    .findFirst()
                    .orElseGet(() -> root.join("customer"));

            return builder.like(builder.upper(customerJoin.get(field)), "%" + value.toUpperCase() + "%");
        };
    }

    public static Specification<Order> hasCustomerName(String name) {
        return trStringFieldLike("name", name);
    }

    public static Specification<Order> hasCustomerLastName(String lastName) {
        return trStringFieldLike("lastName", lastName);
    }

    public static Specification<Order> isActive() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

}
