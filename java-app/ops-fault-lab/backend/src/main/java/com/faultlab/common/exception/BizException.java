package com.faultlab.common.exception;

/**
 * 业务异常用于向前端返回明确、可读的操作失败原因。
 */
public class BizException extends RuntimeException {

    private final String code;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
