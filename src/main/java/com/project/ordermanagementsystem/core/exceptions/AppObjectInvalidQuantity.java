package com.project.ordermanagementsystem.core.exceptions;

public class AppObjectInvalidQuantity extends AppGenericException {
    private static final String DEFAULT_CODE = "InvalidQuantity";

    public AppObjectInvalidQuantity(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
