package com.f0rsaken.imageprocessor.common;

public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;

    public BaseResponse() {}

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> BaseResponse<T> error(ResponseCode code) {
        return new BaseResponse<>(code.getCode(), code.getMessage(), null);
    }

    public static <T> BaseResponse<T> error(ResponseCode code, String customMessage) {
        return new BaseResponse<>(code.getCode(), customMessage, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
