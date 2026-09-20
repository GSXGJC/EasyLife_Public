package com.gsx.dto;

public class RegDto {
    private String email;
    private String password;
    private String nickName;
    private String vertifiCode;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVertifiCode() {
        return vertifiCode;
    }

    public void setVertifiCode(String vertifiCode) {
        this.vertifiCode = vertifiCode;
    }
}
