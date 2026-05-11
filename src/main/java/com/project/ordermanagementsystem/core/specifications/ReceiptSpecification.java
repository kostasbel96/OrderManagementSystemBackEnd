package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.core.utils.SpecificationUtils;
import com.project.ordermanagementsystem.dto.FilterRequest;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Receipt;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ReceiptSpecification {

    private ReceiptSpecification() {
    }

    public static Specification<Receipt> globalSearch(String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }

            Join<Receipt, Customer> customer = root.join("customer", JoinType.LEFT);
            query.distinct(true);

            // Numeric search (customer id or receipt id)
            if (value.matches("\\d+")) {
                Long id = Long.parseLong(value);
                return cb.or(
                        cb.equal(root.get("id"), id),
                        cb.equal(customer.get("id"), id)
                );
            }

            String normalized = SpecificationUtils.normalizeGreek(value.toLowerCase().trim());
            String like = "%" + normalized + "%";

            // Customer fields
            Expression<String> customerName = cb.function(
                    "replace",
                    String.class,
                    cb.lower(customer.get("name")),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            Expression<String> customerLastName = cb.function(
                    "replace",
                    String.class,
                    cb.lower(customer.get("lastName")),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            Expression<String> fullName = cb.function(
                    "replace",
                    String.class,
                    cb.lower(
                            cb.concat(
                                    cb.concat(
                                            cb.coalesce(customer.get("name"), ""),
                                            " "
                                    ),
                                    cb.coalesce(customer.get("lastName"), "")
                            )
                    ),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            // Receipt fields
            Expression<String> notes = cb.function(
                    "replace",
                    String.class,
                    cb.lower(root.get("notes")),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            return cb.or(
                    cb.like(customerName, like),
                    cb.like(customerLastName, like),
                    cb.like(fullName, like),
                    cb.like(notes, like),
                    cb.like(customer.get("phoneNumber1").as(String.class), like),
                    cb.like(customer.get("phoneNumber2").as(String.class), like)
            );
        };
    }

    public static Specification<Receipt> fromFilter(FilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null || filter.getField() == null || filter.getValue() == null) {
                return cb.conjunction();
            }

            String field = filter.getField();
            String operator = filter.getOperator();
            Object value = filter.getValue();

            if (operator == null) return cb.conjunction();

            // Customer filter
            if ("customer".equals(field)) {
                Join<Receipt, Customer> customer = root.join("customer", JoinType.LEFT);
                return customerPredicate(cb, customer, operator, value.toString());
            }

            // Amount filter
            if ("amount".equals(field)) {
                return SpecificationUtils.numericFilter(root, cb, field, operator, value);
            }

            // Date filter
            if ("date".equals(field) || "createdAt".equals(field)) {
                return SpecificationUtils.dateFilter(root, cb, field, operator, value);
            }

            // String filters
            Expression<?> expression = root.get(field);
            return switch (operator) {
                case "contains" -> cb.like(
                        cb.lower(expression.as(String.class)),
                        "%" + value.toString().toLowerCase() + "%"
                );
                case "doesNotContain" -> cb.notLike(
                        cb.lower(expression.as(String.class)),
                        "%" + value.toString().toLowerCase() + "%"
                );
                case "startsWith" -> cb.like(
                        cb.lower(expression.as(String.class)),
                        value.toString().toLowerCase() + "%"
                );
                case "endsWith" -> cb.like(
                        cb.lower(expression.as(String.class)),
                        "%" + value.toString().toLowerCase()
                );
                case "equals" -> cb.equal(
                        expression,
                        SpecificationUtils.castValue(expression, value.toString())
                );
                case "doesNotEqual" -> cb.notEqual(
                        expression,
                        SpecificationUtils.castValue(expression, value.toString())
                );
                default -> cb.conjunction();
            };
        };
    }

    public static Specification<Receipt> isActive() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

    private static Predicate customerPredicate(
            CriteriaBuilder cb,
            Join<Receipt, Customer> customer,
            String operator,
            String value
    ) {
        String like = "%" + value.toLowerCase() + "%";
        return switch (operator) {
            case "contains" -> cb.or(
                    cb.like(cb.lower(customer.get("name")), like),
                    cb.like(cb.lower(customer.get("lastName")), like),
                    cb.like(cb.lower(customer.get("email")), like)
            );
            case "startsWith" -> cb.like(cb.lower(customer.get("name")), value.toLowerCase() + "%");
            case "equals" -> cb.equal(cb.lower(customer.get("name")), value.toLowerCase());
            default -> cb.conjunction();
        };
    }
}
