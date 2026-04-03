package com.moneibakang.informationretrievalbackend.api;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private Integer statusCode;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data, String message, Integer statusCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, 200);
    }

    public static <T> ApiResponse<T> ok(T data, String message, int statusCode) {
        return new ApiResponse<>(true, data, message, statusCode);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, 500);
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return new ApiResponse<>(false, null, message, statusCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}
