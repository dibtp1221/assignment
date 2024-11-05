package com.musinsa.assignment.exhandle.exception;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    private final String detail;
    public UserException(String message, String detail) {
        super(message);
        this.detail = detail;
    }
}
