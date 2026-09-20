package com.gsx.Utils.ResultUtil;

/**
 * 统一响应状态码
 * 规范：200成功，4xx客户端错误，5xx服务端错误
 */
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 (4xx)
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期，请重新登录"),
    FORBIDDEN(403, "没有操作权限，请联系管理员"),
    NOT_FOUND(404, "请求资源不存在"),
    VALIDATE_FAILED(422, "参数校验失败"),

    // 服务端错误 (5xx)
    INTERNAL_SERVER_ERROR(500, "服务器内部错误，请稍后重试"),
    BUSINESS_ERROR(501, "业务处理失败"),

    // 自定义业务错误 (用户相关 1000+)
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "电话或密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被冻结，请联系管理员"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    TOKEN_INVALID(1005, "无效的Token"),
    PHONE_NUMBER_USED(1006,"电话已注册"),
    EMAIL_USED(1007,"邮箱已注册"),
    EMAIL_CODE_WRONG(1008,"验证码错误"),
    EMAIL_CODE_SENT(1009,"验证码已发送，请稍后重试" );
    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}