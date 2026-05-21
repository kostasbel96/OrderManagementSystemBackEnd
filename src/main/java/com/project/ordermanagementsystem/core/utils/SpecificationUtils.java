package com.project.ordermanagementsystem.core.utils;

import com.project.ordermanagementsystem.model.Payment;
import com.project.ordermanagementsystem.model.Receipt;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SpecificationUtils {

    private SpecificationUtils(){}

    public static String normalizeGreek(String input) {
        return input
                .toLowerCase()
                .replace("ς", "σ");
    }

    public static LocalDateTime parseDate(String value) {
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

    public static Predicate numericFilter(Root<?> root, CriteriaBuilder cb, String field, String operator, Object value) {
        return switch (operator) {
            case "=" -> cb.equal(root.get(field), new BigDecimal(value.toString()));
            case ">" -> cb.greaterThan(root.get(field), new BigDecimal(value.toString()));
            case ">=" -> cb.greaterThanOrEqualTo(root.get(field), new BigDecimal(value.toString()));
            case "<" -> cb.lessThan(root.get(field), new BigDecimal(value.toString()));
            case "<=" -> cb.lessThanOrEqualTo(root.get(field), new BigDecimal(value.toString()));
            default -> cb.conjunction();
        };
    }


    public static Predicate dateFilter(Root<?> root, CriteriaBuilder cb, String field, String operator, Object value) {
        LocalDateTime date = parseDate(value.toString());
        return switch (operator) {
            case "is" -> cb.equal(root.get(field), date);
            case "after" -> cb.greaterThan(root.get(field), date);
            case "before" -> cb.lessThan(root.get(field), date);
            default -> cb.conjunction();
        };
    }


    // ---------------- TYPE CASTING ----------------
    public static Object castValue(Expression<?> expression, String value) {

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
