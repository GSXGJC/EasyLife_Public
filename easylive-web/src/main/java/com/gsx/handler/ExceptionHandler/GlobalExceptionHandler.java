package com.gsx.handler.ExceptionHandler;

import com.gsx.Utils.ResultUtil.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public Result<Void> handlerServiceException(ServiceException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handlerServiceException(Exception e) {
        if (e instanceof MaxUploadSizeExceededException) {
            return Result.error("文件过大，请上传不超过 100MB 的文件");
        }
        e.printStackTrace();
        return Result.error("系统繁忙,请稍后再试");
    }
}
