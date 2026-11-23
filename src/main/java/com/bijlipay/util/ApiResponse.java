package com.bijlipay.util;

import org.springframework.http.HttpStatus;

public class ApiResponse {
    public HttpStatus status;
    public String message;
    public Object data;

    public ApiResponse() {
        this.status = null;
        this.message = null;
        this.data = null;
    }

    public ApiResponse(HttpStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}