package com.gsx.handler.ExceptionHandler;

import com.gsx.Utils.ResultUtil.ResultCode;

public class ServiceException extends RuntimeException {
    private final Integer code;
    public ServiceException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public ServiceException(String message) {
        super(message);
        this.code = 501;
    }

    public Integer getCode() {
        return code;
    }
}
