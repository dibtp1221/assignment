package com.musinsa.assignment.domain.dto.inner;

public record ResultDto<T>(boolean success, String message, T result) {
}
