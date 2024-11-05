package com.musinsa.assignment.exhandle.advice;

import com.musinsa.assignment.exhandle.ErrorResult;
import com.musinsa.assignment.exhandle.exception.NeedMonitorException;
import com.musinsa.assignment.exhandle.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ExControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserException.class)
    public ErrorResult userExHandler(UserException e) {
        return new ErrorResult("USER-EX", e.getMessage(), e.getDetail());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult exHandler(Exception e) {
        return new ErrorResult("EX", "내부 오류", "시각: " + LocalDateTime.now() + " " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult needMonitorExHandler(NeedMonitorException e) {
        log.error("[NeedMonitorException] ", e);
        return new ErrorResult("DATA-EX", "내부 오류", "서버 확인 중");
    }

}
