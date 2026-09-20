package com.gsx.controller;

import com.gsx.Utils.ResultUtil.Result;
import com.gsx.dto.LoginDto;
import com.gsx.dto.RegDto;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gsx.service.impl.UserServiceImpl;
import com.gsx.vo.LoginVo;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    UserServiceImpl userServiceImpl;
    public UserController(@Autowired UserServiceImpl userServiceImpl ) {
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/login")
    public Result<LoginVo> login(@RequestBody LoginDto loginDto){
        return Result.success(userServiceImpl.login(loginDto));
    }

    @PostMapping("/logout")
    public Result<LoginVo> logout(){
        userServiceImpl.logout();
        return Result.success();
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegDto regDto){
        userServiceImpl.register(regDto);
        return Result.success();
    }

}
