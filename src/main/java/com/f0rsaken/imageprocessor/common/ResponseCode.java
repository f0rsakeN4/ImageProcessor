package com.f0rsaken.imageprocessor.common;

public enum ResponseCode {
    SUCCESS(200, "Operation Successful"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    // 文件异常
    VALID_FILE(2001, "Valid File"),
    FILE_UPLOAD_ERROR(4000, "File upload failed"),
    INVALID_FILE(4001, "Invalid File"),
    UNSUPPORTED_FILE_TYPE(4002, "Unsupported File Type"),
    FILE_TOO_LARGE(4003, "File Too Large");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

