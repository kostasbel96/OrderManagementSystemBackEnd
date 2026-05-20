package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.core.utils.SpecificationUtils;
import com.project.ordermanagementsystem.dto.FilterRequest;
import com.project.ordermanagementsystem.model.*;
import com.project.ordermanagementsystem.model.Order;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class PurchaseOrderSpecification {

    private PurchaseOrderSpecification(){

    }

    public static Specification<PurchaseOrder> globalSearch(String value) {
        return (root, query, cb) -> {

            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }

            Join<Order, Supplier> supplier = root.join("supplier", JoinType.LEFT);

            query.distinct(true);

            String normalized = SpecificationUtils.normalizeGreek(value.toLowerCase().trim());
            String like = "%" + normalized + "%";

            // numeric search (supplier id)
            if (value.matches("\\d+")) {
                Long id = Long.parseLong(value);
                return cb.equal(supplier.get("id"), id);
            }

            // ---------------- NAME ----------------
            Expression<String> name = cb.function(
                    "replace",
                    String.class,
                    cb.lower(supplier.get("name")),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            // ---------------- SUPPLIER FIELDS ----------------

            Expression<String> phone1 = supplier.get("phoneNumber1").as(String.class);
            Expression<String> phone2 = supplier.get("phoneNumber2").as(String.class);
            Expression<String> email = supplier.get("email").as(String.class);
            Expression<String> vat = supplier.get("vatNumber").as(String.class);

            return cb.or(

                    // SUPPLIER NAME
                    cb.like(name, like),

                    // PHONE
                    cb.like(phone1, like),
                    cb.like(phone2, like),

                    // SUPPLIER EMAIL
                    cb.like(email, like),

                    // SUPPLIER VAT
                    cb.like(vat, like)
            );
        };
    }

    public static Specification<PurchaseOrder> fromFilter(FilterRequest filter) {
        return (root, query, cb) -> {

            if (filter == null || filter.getField() == null || filter.getValue() == null) {
                return cb.conjunction();
            }

            String field = filter.getField();
            String operator = filter.getOperator();
            Object value = filter.getValue();

            if (operator == null) return cb.conjunction();

            if ("supplier".equals(field)) {

                Join<Order, Supplier> supplier = root.join("supplier", JoinType.LEFT);

                return supplierPredicate(cb, supplier, operator, value.toString());
            }

            if ("products".equals(field)) {

                Join<Order, OrderItem> items = root.join("items", JoinType.LEFT);
                Join<OrderItem, Product> product = items.join("product", JoinType.LEFT);

                String like = "%" + value.toString().toLowerCase() + "%";

                query.distinct(true);

                return switch (operator) {

                    case "containsProduct" -> cb.like(
                            cb.lower(product.get("name")),
                            like
                    );

                    default -> cb.like(
                            cb.lower(product.get("name")),
                            like
                    );
                };
            }

            // ---------------- STATUS ----------------
            if ("status".equals(field)) {

                return switch (operator) {

                    case "is" -> cb.equal(root.get("status"), value);

                    case "not" -> cb.notEqual(root.get("status"), value);

                    case "isAnyOf" -> {
                        List<?> list = (List<?>) value;

                        CriteriaBuilder.In<Object> in = cb.in(root.get("status"));

                        for (Object v : list) {
                            in.value(v);
                        }

                        yield in;
                    }

                    default -> cb.conjunction();
                };
            }
            // ---------------- PAYMENT STATUS ----------------
            if ("paymentStatus".equals(field)) {

                return switch (operator) {

                    case "is" -> cb.equal(root.get("paymentStatus"), value);

                    case "not" -> cb.notEqual(root.get("paymentStatus"), value);

                    case "isAnyOf" -> {
                        List<?> list = (List<?>) value;

                        CriteriaBuilder.In<Object> in = cb.in(root.get("paymentStatus"));

                        for (Object v : list) {
                            in.value(v);
                        }

                        yield in;
                    }

                    default -> cb.conjunction();
                };
            }

            // ---------------- DATE FILTER ----------------
            if ("date".equals(field) || "createdAt".equals(field)) {

                LocalDateTime date = SpecificationUtils.parseDate(value.toString());

                return switch (operator) {

                    case "is" -> cb.equal(root.get(field), date);

                    case "not" -> cb.notEqual(root.get(field), date);

                    case "after" -> cb.greaterThan(root.get(field), date);

                    case "onOrAfter" -> cb.greaterThanOrEqualTo(root.get(field), date);

                    case "before" -> cb.lessThan(root.get(field), date);

                    case "onOrBefore" -> cb.lessThanOrEqualTo(root.get(field), date);

                    default -> cb.conjunction();
                };
            }

            Expression<?> expression = root.get(field);

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

    public static Specification<PurchaseOrder> isActive() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

    private static Predicate supplierPredicate(
            CriteriaBuilder cb,
            Join<Order, Supplier> supplier,
            String operator,
            String value
    ) {
        String like = "%" + value.toLowerCase() + "%";

        return switch (operator) {

            case "contains" -> cb.or(
                    cb.like(cb.lower(supplier.get("name")), like),
                    cb.like(cb.lower(supplier.get("email")), like),
                    cb.like(supplier.get("phoneNumber1"), like),
                    cb.like(supplier.get("phoneNumber2"), like),
                    cb.like(supplier.get("email"), like)
            );

            case "startsWith" -> cb.or(
                    cb.like(cb.lower(supplier.get("name")), value.toLowerCase() + "%")
            );

            case "endsWith" -> cb.or(
                    cb.like(cb.lower(supplier.get("name")), "%" + value.toLowerCase())
            );

            case "equals" -> cb.or(
                    cb.equal(cb.lower(supplier.get("name")), value.toLowerCase())
            );

            case "doesNotEqual" -> cb.not(
                    cb.or(
                        cb.equal(cb.lower(supplier.get("name")), value.toLowerCase())
                    )
            );

            default -> cb.or(
                    cb.like(cb.lower(supplier.get("name")), like)
            );
        };
    }

}
