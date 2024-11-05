package com.musinsa.assignment.exhandle;

import lombok.Getter;

@Getter
public class ErrorResult {
    private final String code;
    private final String message;
    private final String detail;

    public ErrorResult(String code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }
}