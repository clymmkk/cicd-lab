package com.faultlab.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式，方便前端和 Postman 使用一致的数据结构。
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(true, "OK", "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, "OK", message, data);
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(false, code, message, null);
    }
}
