package com.example.baoxiao.service;

/**
 * 业务异常，携带 HTTP 状态码，便于统一转换为响应。
 */
public class BizException extends RuntimeException {

    private final int status;

    public BizException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static BizException forbidden(String message) {
        return new BizException(403, message);
    }

    public static BizException notFound(String message) {
        return new BizException(404, message);
    }

    public static BizException badRequest(String message) {
        return new BizException(400, message);
    }

    public int getStatus() {
        return status;
    }
}
