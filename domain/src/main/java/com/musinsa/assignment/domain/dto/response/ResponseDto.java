package com.musinsa.assignment.domain.dto.response;

import lombok.Getter;

@Getter
public class ResponseDto<T> {
    private final boolean success;
    private final String message;
    private final T data;

    public ResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
