package com.gsx.Utils.ResultUtil;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 * 适用于所有前后端分离的接口返回
 */
public class Result<T> implements Serializable {
    // 状态码
    private Integer code;
    // 提示信息
    private String message;
    // 返回数据（泛型，支持任意类型）
    private T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // -------------------- 成功响应 (200) --------------------

    /**
     * 成功响应，无返回数据
     */
    public static <T> Result<T> success() {
        return new Result<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应，带返回数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应，自定义消息（极少数场景使用）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // -------------------- 失败响应 (根据枚举) --------------------

    /**
     * 根据枚举返回错误信息
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<T>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 根据枚举返回错误信息，并携带额外数据（用于校验失败等场景）
     */
    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    /**
     * 自定义错误码和消息（不推荐，尽量使用枚举，保持规范）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>(code, message, null);
    }

    /**
     * 自定义错误消息，状态码默认 500（业务异常通用）
     */
    public static <T> Result<T> error(String message) {
        return new Result<T>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message, null);
    }


    // -------------------- Getter & Setter --------------------

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
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
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}