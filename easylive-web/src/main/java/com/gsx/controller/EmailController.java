package com.gsx.controller;

import com.gsx.Utils.RedisUtil;
import com.gsx.Utils.ResultUtil.Result;
import com.gsx.service.EmailService;
import com.gsx.service.impl.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EmailController {

    EmailServiceImpl emailServiceImpl;
    EmailController(@Autowired EmailServiceImpl emailServiceImpl){
        this.emailServiceImpl = emailServiceImpl;
    }

    @PostMapping("/email")
    public Result sendEmail(@RequestParam String email){
        emailServiceImpl.sendEmail(email);
        return Result.success();
    }
}
