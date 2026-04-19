package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.dto.FilterRequest;
import com.project.ordermanagementsystem.model.Customer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class CustomerSpecification {

    private CustomerSpecification() {}

    // ---------------- GLOBAL SEARCH ----------------
    public static Specification<Customer> globalSearch(String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return cb.conjunction();

            String like = "%" + value.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(
                            cb.lower(
                                    cb.concat(
                                            cb.concat(root.get("name"), " "),
                                            root.get("lastName")
                                    )
                            ),
                            like
                    )
            );
        };
    }

    // ---------------- ACTIVE ----------------
    public static Specification<Customer> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    // ---------------- FILTER ENGINE ----------------
    public static Specification<Customer> fromFilter(FilterRequest filter) {
        return (root, query, cb) -> {

            if (filter == null || filter.getField() == null) {
                return cb.conjunction();
            }

            String field = filter.getField();
            String operator = filter.getOperator();
            String value = filter.getValue();

            Expression<?> expression = root.get(field);

            if (operator == null) return cb.conjunction();
            if (value == null) return cb.conjunction();

            return switch (operator) {

                // ---------------- STRING OPS ----------------
                case "contains" -> cb.like(
                        cb.lower(expression.as(String.class)),
                        "%" + value.toLowerCase() + "%"
                );

                case "doesNotContain" -> cb.notLike(
                        cb.lower(expression.as(String.class)),
                        "%" + value.toLowerCase() + "%"
                );

                case "startsWith" -> cb.like(
                        cb.lower(expression.as(String.class)),
                        value.toLowerCase() + "%"
                );

                case "endsWith" -> cb.like(
                        cb.lower(expression.as(String.class)),
                        "%" + value.toLowerCase()
                );

                case "equals", "=", "equal" -> cb.equal(
                        expression,
                        castValue(expression, value)
                );

                case "doesNotEqual" -> cb.notEqual(
                        expression,
                        castValue(expression, value)
                );

                // ---------------- NULL / EMPTY ----------------
                case "isEmpty" -> cb.or(
                        cb.isNull(expression),
                        cb.equal(expression.as(String.class), "")
                );

                case "isNotEmpty" -> cb.and(
                        cb.isNotNull(expression),
                        cb.notEqual(expression.as(String.class), "")
                );

                // ---------------- LIST ----------------
                case "isAnyOf" -> {
                    List<String> values = Arrays.stream(value.split(","))
                            .map(String::trim)
                            .toList();

                    CriteriaBuilder.In<Object> in = cb.in(expression);
                    for (String v : values) {
                        in.value(castValue(expression, v));
                    }

                    yield in;
                }

                // ---------------- NUMERIC / COMPARISON ----------------
                case ">", "greaterThan" -> cb.greaterThan(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value)
                );

                case ">=", "greaterThanOrEqual" -> cb.greaterThanOrEqualTo(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value)
                );

                case "<", "lessThan" -> cb.lessThan(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value)
                );

                case "<=", "lessThanOrEqual" -> cb.lessThanOrEqualTo(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value)
                );

                // ---------------- NOT EQUAL (extra safety) ----------------
                case "!=", "notEqual" -> cb.notEqual(
                        expression,
                        castValue(expression, value)
                );

                // ---------------- DEFAULT ----------------
                default -> cb.conjunction();
            };
        };
    }

    // ---------------- TYPE CASTING ----------------
    private static Object castValue(Expression<?> expression, String value) {

        Class<?> type = expression.getJavaType();

        if (value == null) return null;

        if (type.equals(String.class)) return value;

        if (type.equals(Integer.class)) return Integer.valueOf(value);

        if (type.equals(Long.class)) return Long.valueOf(value);

        if (type.equals(Double.class)) return Double.valueOf(value);

        if (type.equals(BigDecimal.class)) return new BigDecimal(value);

        if (type.equals(Boolean.class)) return Boolean.valueOf(value);

        if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, value);
        }

        return value;
    }
}