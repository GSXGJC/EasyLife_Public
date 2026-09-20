package com.gsx.vo;

import com.gsx.Utils.JwtUtil;

public class LoginVo {
    String nickName;
    String userId;
    String token;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public LoginVo(String nickName, String userId, String token) {
        this.nickName = nickName;
        this.userId = userId;
        this.token = token;
    }
}
