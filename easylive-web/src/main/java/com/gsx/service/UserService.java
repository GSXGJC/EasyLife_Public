package com.gsx.service;

import com.gsx.dto.LoginDto;
import com.gsx.dto.RegDto;
import com.gsx.vo.LoginVo;


public interface UserService {
    LoginVo login(LoginDto loginDto);
    void logout();
    void register(RegDto regDto);
}
