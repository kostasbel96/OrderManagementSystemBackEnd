package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.dto.FilterRequest;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Order;
import com.project.ordermanagementsystem.model.OrderItem;
import com.project.ordermanagementsystem.model.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class OrderSpecification {

    private OrderSpecification(){

    }

    public static Specification<Order> globalSearch(String value) {
        return (root, query, cb) -> {

            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }



            Join<Order, Customer> customer = root.join("customer", JoinType.LEFT);

            query.distinct(true);

            // numeric search (customer id)
            if (value.matches("\\d+")) {
                Long id = Long.parseLong(value);
                return cb.equal(customer.get("id"), id);
            }

            String normalized = normalizeGreek(value.toLowerCase().trim());
            String like = "%" + normalized + "%";

            // ---------------- CUSTOMER FIELDS ----------------
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

            // ---------------- ORDER FIELDS ----------------
            Expression<String> address = cb.function(
                    "replace",
                    String.class,
                    cb.lower(root.get("address")),
                    cb.literal("ς"),
                    cb.literal("σ")
            );

            Expression<String> phone1 = customer.get("phoneNumber1").as(String.class);
            Expression<String> phone2 = customer.get("phoneNumber2").as(String.class);

            return cb.or(

                    // CUSTOMER
                    cb.like(customerName, like),
                    cb.like(customerLastName, like),
                    cb.like(fullName, like),

                    // PHONE
                    cb.like(phone1, like),
                    cb.like(phone2, like),

                    // ORDER
                    cb.like(address, like)
            );
        };
    }

    public static Specification<Order> fromFilter(FilterRequest filter) {
        return (root, query, cb) -> {

            if (filter == null || filter.getField() == null || filter.getValue() == null) {
                return cb.conjunction();
            }

            String field = filter.getField();
            String operator = filter.getOperator();
            String value = filter.getValue().toString();

            if (operator == null) return cb.conjunction();
            if (value == null) return cb.conjunction();

            if ("customer".equals(field)) {

                Join<Order, Customer> customer = root.join("customer", JoinType.LEFT);

                return customerPredicate(cb, customer, operator, value);
            }

            if ("products".equals(field)) {

                Join<Order, OrderItem> items = root.join("items", JoinType.LEFT);
                Join<OrderItem, Product> product = items.join("product", JoinType.LEFT);

                String like = "%" + value.toLowerCase() + "%";

                query.distinct(true);

                return switch (operator) {

                    case "containsProduct", "contains", "equals" -> cb.like(
                            cb.lower(product.get("name")),
                            like
                    );

                    default -> cb.like(
                            cb.lower(product.get("name")),
                            like
                    );
                };
            }

            if ("payment".equals(field)) {

                CriteriaBuilder.Case<String> paymentCase = cb.selectCase();

                Expression<String> paymentExpr = paymentCase
                        .when(cb.equal(root.get("deposit"), BigDecimal.ZERO), "UNPAID")
                        .when(cb.lessThan(root.get("deposit"), root.get("total")), "PARTIAL")
                        .otherwise("PAID");

                return switch (operator) {

                    case "is", "equals" ->
                            cb.equal(paymentExpr, value.toUpperCase());

                    case "not" ->
                            cb.notEqual(paymentExpr, value.toUpperCase());

                    default -> cb.conjunction();
                };
            }

            Expression<?> expression = root.get(field);

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

                //------------------ DATE ---------------------
                case "is" -> cb.equal(
                        root.get(field),
                        parseDate(value)
                );

                case "not" -> cb.notEqual(
                        root.get(field),
                        parseDate(value)
                );

                case "after" -> cb.greaterThan(
                        root.get(field),
                        parseDate(value)
                );

                case "onOrAfter" -> cb.greaterThanOrEqualTo(
                        root.get(field),
                        parseDate(value)
                );

                case "before" -> cb.lessThan(
                        root.get(field),
                        parseDate(value)
                );

                case "onOrBefore" -> cb.lessThanOrEqualTo(
                        root.get(field),
                        parseDate(value)
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

    public static Specification<Order> isActive() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

    private static Predicate customerPredicate(
            CriteriaBuilder cb,
            Join<Order, Customer> customer,
            String operator,
            String value
    ) {
        String like = "%" + value.toLowerCase() + "%";

        return switch (operator) {

            case "contains" -> cb.or(
                    cb.like(cb.lower(customer.get("name")), like),
                    cb.like(cb.lower(customer.get("lastName")), like),
                    cb.like(
                            cb.lower(
                                    cb.concat(
                                            cb.concat(customer.get("name"), " "),
                                            customer.get("lastName")
                                    )
                            ),
                            like
                    ),
                    cb.like(cb.lower(customer.get("email")), like),
                    cb.like(customer.get("phoneNumber1"), like),
                    cb.like(customer.get("phoneNumber2"), like)
            );

            case "startsWith" -> cb.or(
                    cb.like(cb.lower(customer.get("name")), value.toLowerCase() + "%"),
                    cb.like(
                            cb.lower(
                                    cb.concat(
                                            cb.concat(customer.get("name"), " "),
                                            customer.get("lastName")
                                    )
                            ),
                            value.toLowerCase() + "%"
                    ),
                    cb.like(cb.lower(customer.get("lastName")), value.toLowerCase() + "%")
            );

            case "endsWith" -> cb.or(
                    cb.like(cb.lower(customer.get("name")), "%" + value.toLowerCase()),
                    cb.like(
                            cb.lower(
                                    cb.concat(
                                            cb.concat(customer.get("name"), " "),
                                            customer.get("lastName")
                                    )
                            ),
                            "%" + value.toLowerCase()
                    ),
                    cb.like(cb.lower(customer.get("lastName")), "%" + value.toLowerCase())
            );

            case "equals", "=", "equal" -> cb.or(
                    cb.equal(cb.lower(customer.get("name")), value.toLowerCase()),
                    cb.like(
                            cb.lower(
                                    cb.concat(
                                            cb.concat(customer.get("name"), " "),
                                            customer.get("lastName")
                                    )
                            ),
                            value.toLowerCase()
                    ),
                    cb.equal(cb.lower(customer.get("lastName")), value.toLowerCase())
            );

            case "doesNotEqual" -> cb.not(
                    cb.or(
                            cb.equal(cb.lower(customer.get("name")), value.toLowerCase()),
                            cb.like(
                                    cb.lower(
                                            cb.concat(
                                                    cb.concat(customer.get("name"), " "),
                                                    customer.get("lastName")
                                            )
                                    ),
                                    value.toLowerCase()
                            ),
                            cb.equal(cb.lower(customer.get("lastName")), value.toLowerCase())
                    )
            );

            default -> cb.or(
                    cb.like(cb.lower(customer.get("name")), like),
                    cb.like(cb.lower(customer.get("lastName")), like)
            );
        };
    }

    private static LocalDateTime parseDate(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            try {
                return java.time.Instant.parse(value)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid date: " + value);
            }
        }
    }

    private static String normalizeGreek(String input) {
        return input
                .toLowerCase()
                .replace("ς", "σ");
    }

}
