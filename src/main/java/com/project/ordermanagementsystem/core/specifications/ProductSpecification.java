package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.core.utils.SpecificationUtils;
import com.project.ordermanagementsystem.dto.FilterRequest;
import com.project.ordermanagementsystem.model.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> totalProductsByDate(LocalDate date) {
        return (root, query, cb) -> cb.or(
                cb.between(
                        root.get("createdAt"),
                        date.atStartOfDay(),
                        date.atTime(LocalTime.MAX)
                ),
                cb.between(
                        root.get("updatedAt"),
                        date.atStartOfDay(),
                        date.atTime(LocalTime.MAX)
                )
        );
    }

    public static Specification<Product> stockLessThanOrEqual(int threshold) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("quantity"), threshold);
    }

    // ---------------- GLOBAL SEARCH ----------------
    public static Specification<Product> globalSearch(String value) {
        return (root, query, cb) -> {

            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }

            String normalized = SpecificationUtils.normalizeGreek(value.toLowerCase().trim());
            String like = "%" + normalized + "%";

            query.distinct(true);

            // ---------------- NAME ----------------
            Expression<String> name = cb.function(
                    "replace",
                    String.class,
                    cb.lower(root.get("name")),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            // ---------------- DESCRIPTION ----------------
            Expression<String> description = cb.function(
                    "replace",
                    String.class,
                    cb.lower(root.get("description")),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            return cb.or(
                    cb.like(name, like),
                    cb.like(description, like)
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

            if (filter == null || filter.getField() == null || filter.getValue() == null) {
                return cb.conjunction();
            }

            String field = filter.getField();
            String operator = filter.getOperator();
            Object value = filter.getValue();

            Expression<?> expression = root.get(field);

            if (operator == null) return cb.conjunction();

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
                        SpecificationUtils.castValue(expression, value.toString())
                );

                case "doesNotEqual" -> cb.notEqual(
                        expression,
                        SpecificationUtils.castValue(expression, value.toString())
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
                        in.value(SpecificationUtils.castValue(expression, v));
                    }

                    yield in;
                }

                // ---------------- NUMERIC / COMPARISON ----------------
                case "=" -> cb.equal(
                        root.get(field),
                        SpecificationUtils.castValue(root.get(field), value.toString())
                );
                case ">" -> cb.greaterThan(
                        root.get(field),
                        (Comparable) SpecificationUtils.castValue(root.get(field), value.toString())
                );

                case ">=" -> cb.greaterThanOrEqualTo(
                        root.get(field),
                        (Comparable) SpecificationUtils.castValue(root.get(field), value.toString())
                );

                case "<" -> cb.lessThan(
                        root.get(field),
                        (Comparable) SpecificationUtils.castValue(root.get(field), value.toString())
                );

                case "<=" -> cb.lessThanOrEqualTo(
                        root.get(field),
                        (Comparable) SpecificationUtils.castValue(root.get(field), value.toString())
                );

                // ---------------- NOT EQUAL (extra safety) ----------------
                case "!=" -> cb.notEqual(
                        expression,
                        SpecificationUtils.castValue(expression, value.toString())
                );

                // ---------------- DEFAULT ----------------
                default -> cb.conjunction();
            };
        };
    }
}