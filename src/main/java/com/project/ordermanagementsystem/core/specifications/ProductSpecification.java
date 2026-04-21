package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.dto.FilterRequest;
import com.project.ordermanagementsystem.model.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ProductSpecification {

    private ProductSpecification() {}

    private static String normalizeGreek(String input) {
        return input
                .toLowerCase()
                .replace("ά", "α")
                .replace("έ", "ε")
                .replace("ή", "η")
                .replace("ί", "ι")
                .replace("ό", "ο")
                .replace("ύ", "υ")
                .replace("ώ", "ω")
                .replace("ς", "σ");
    }

    // ---------------- GLOBAL SEARCH ----------------
    public static Specification<Product> globalSearch(String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return cb.conjunction();

            String like = "%" + normalizeGreek(value.toLowerCase()) + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    // ---------------- ACTIVE ----------------
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    // ---------------- FILTER ENGINE ----------------
    public static Specification<Product> fromFilter(FilterRequest filter) {
        return (root, query, cb) -> {

            if (filter == null || filter.getField() == null) {
                return cb.conjunction();
            }

            String field = filter.getField();
            String operator = filter.getOperator();
            Object value = filter.getValue();

            Expression<?> expression = root.get(field);

            if (operator == null) return cb.conjunction();
            if (value == null) return cb.conjunction();

            return switch (operator) {

                // ---------------- STRING OPS ----------------
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
                        castValue(expression, value.toString())
                );

                case "doesNotEqual" -> cb.notEqual(
                        expression,
                        castValue(expression, value.toString())
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

                    List<?> list = (List<?>) value;

                    List<String> values = list.stream()
                            .map(Object::toString)
                            .toList();
                    CriteriaBuilder.In<Object> in = cb.in(expression);
                    for (String v : values) {
                        in.value(castValue(expression, v));
                    }

                    yield in;
                }

                // ---------------- NUMERIC / COMPARISON ----------------
                case "=" -> cb.equal(
                        root.get(field),
                        castValue(root.get(field), value.toString())
                );
                case ">" -> cb.greaterThan(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value.toString())
                );

                case ">=" -> cb.greaterThanOrEqualTo(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value.toString())
                );

                case "<" -> cb.lessThan(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value.toString())
                );

                case "<=" -> cb.lessThanOrEqualTo(
                        root.get(field),
                        (Comparable) castValue(root.get(field), value.toString())
                );

                // ---------------- NOT EQUAL (extra safety) ----------------
                case "!=" -> cb.notEqual(
                        expression,
                        castValue(expression, value.toString())
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

        if (type.equals(List.class)) return List.of(value);

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