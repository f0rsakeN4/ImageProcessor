package com.f0rsaken.imageprocessor.exception;

import com.f0rsaken.imageprocessor.common.ResponseCode;

public class FileUploadException extends RuntimeException {

    // 响应代码，用于标识具体的错误类型
    private final ResponseCode responseCode;

    public FileUploadException(String message) {
        super(message);
        this.responseCode = ResponseCode.FILE_UPLOAD_ERROR;  // 设置默认的错误代码
    }


    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
        this.responseCode = ResponseCode.FILE_UPLOAD_ERROR;  // 设置默认的错误代码
    }


    public FileUploadException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public FileUploadException(String message, ResponseCode responseCode) {
        super(message);
        this.responseCode = responseCode;
    }


    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
