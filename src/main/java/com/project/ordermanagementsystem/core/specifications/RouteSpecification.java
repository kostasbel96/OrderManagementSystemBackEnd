package com.project.ordermanagementsystem.core.specifications;

import com.project.ordermanagementsystem.core.utils.SpecificationUtils;
import com.project.ordermanagementsystem.dto.FilterRequest;
import com.project.ordermanagementsystem.model.DriverPerson;
import com.project.ordermanagementsystem.model.Route;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RouteSpecification {

    private RouteSpecification() {}

    public static Specification<Route> totalRoutesByDate(LocalDate date) {
        return (root, query, cb) -> cb.equal(
                root.get("date"), date
        );
    }

    // ---------------- ACTIVE ----------------
    public static Specification<Route> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    // ---------------- GLOBAL SEARCH ----------------
    public static Specification<Route> globalSearch(String value) {
        return (root, query, cb) -> {

            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }

            query.distinct(true);

            Join<Route, DriverPerson> driver = root.join("driver", JoinType.LEFT);

            String normalized = SpecificationUtils.normalizeGreek(value.toLowerCase().trim());
            String like = "%" + normalized + "%";

            return cb.or(

                    // route fields
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("notes")), like),

                    // driver fields
                    cb.like(cb.lower(driver.get("name")), like),
                    cb.like(cb.lower(driver.get("lastName")), like)
            );
        };
    }

    // ---------------- FILTERS ----------------
    public static Specification<Route> fromFilter(FilterRequest filter) {

        return (root, query, cb) -> {

            if (filter == null ||
                    filter.getField() == null ||
                    filter.getValue() == null) {
                return cb.conjunction();
            }

            String field = filter.getField();
            String operator = filter.getOperator();
            Object value = filter.getValue();

            if (operator == null) return cb.conjunction();

            // ---------------- DRIVER FILTER ----------------
            if ("driver".equals(field)) {

                Join<Route, DriverPerson> driver = root.join("driver", JoinType.LEFT);

                return driverPredicate(cb, driver, operator, value.toString());
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

            // ---------------- STRING / GENERIC ----------------
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
                        value
                );

                case "doesNotEqual" -> cb.notEqual(
                        expression,
                        value
                );

                default -> cb.conjunction();
            };
        };
    }

    // ---------------- DRIVER PREDICATE ----------------
    private static Predicate driverPredicate(
            CriteriaBuilder cb,
            Join<Route, DriverPerson> driver,
            String operator,
            String value
    ) {

        String like = "%" + value.toLowerCase() + "%";

        return switch (operator) {

            case "contains" -> cb.or(
                    cb.like(cb.lower(driver.get("name")), like),
                    cb.like(cb.lower(driver.get("lastName")), like)
            );

            case "startsWith" -> cb.or(
                    cb.like(cb.lower(driver.get("name")), value.toLowerCase() + "%"),
                    cb.like(cb.lower(driver.get("lastName")), value.toLowerCase() + "%")
            );

            case "equals" -> cb.or(
                    cb.equal(cb.lower(driver.get("name")), value.toLowerCase()),
                    cb.equal(cb.lower(driver.get("lastName")), value.toLowerCase())
            );

            default -> cb.like(cb.lower(driver.get("name")), like);
        };
    }

}