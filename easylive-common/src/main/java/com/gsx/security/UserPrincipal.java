package com.gsx.security;

import java.io.Serial;
import java.security.Principal;
import java.io.Serializable;

/**
 * WebSocket 用户身份封装类
 * 将 userId 包装成 Spring Security 识别的 Principal
 */
public class UserPrincipal implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String nickName;

    public UserPrincipal(String userId, String nickName) {
        this.userId = userId;
        this.nickName = nickName;
    }

    @Override
    public String getName() {
        return userId;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        return "UserPrincipal{userId='" + userId + "', nickName='" + nickName + "'}";
    }
}
