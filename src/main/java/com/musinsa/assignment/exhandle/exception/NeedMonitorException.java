package com.musinsa.assignment.exhandle.exception;

import lombok.Getter;

@Getter
public class NeedMonitorException extends RuntimeException {
    private final String location;
    public NeedMonitorException(String location, String message) {
        super(message);
        this.location = location;
    }
}
