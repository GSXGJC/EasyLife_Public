package com.gsx.entity;

import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库表 user_info
 */
public class User {

    /** 用户id（主键） */
    private String userId;

    /*用户电话号码*/
    private String phoneNumber;

    /** 用户昵称 */
    private String nickName;

    /** 用户头像地址 */
    private String avatar;

    /** 用户邮箱 */
    private String email;

    /** 用户密码 */
    private String password;

    /** 用户性别：0男，1女，2未知 */
    private Integer sex;

    /** 出生日期 */
    private String birthday;

    /** 学校 */
    private String school;

    /** 个人简介 */
    private String personalIntroduction;

    /** 注册时间 */
    private LocalDateTime joinTime;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录ip */
    private String lastLoginIp;

    /** 状态：0正常，1禁言 */
    private Integer status;

    /** 空间公告 */
    private String noticeInfo;

    /** 总硬币数 */
    private Integer totalCoinCount;

    /** 当前硬币数 */
    private Integer currentCoinCount;

    /** 主题，默认1 */
    private Integer theme;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPersonalIntroduction() {
        return personalIntroduction;
    }

    public void setPersonalIntroduction(String personalIntroduction) {
        this.personalIntroduction = personalIntroduction;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNoticeInfo() {
        return noticeInfo;
    }

    public void setNoticeInfo(String noticeInfo) {
        this.noticeInfo = noticeInfo;
    }

    public Integer getTotalCoinCount() {
        return totalCoinCount;
    }

    public void setTotalCoinCount(Integer totalCoinCount) {
        this.totalCoinCount = totalCoinCount;
    }

    public Integer getCurrentCoinCount() {
        return currentCoinCount;
    }

    public void setCurrentCoinCount(Integer currentCoinCount) {
        this.currentCoinCount = currentCoinCount;
    }

    public Integer getTheme() {
        return theme;
    }

    public void setTheme(Integer theme) {
        this.theme = theme;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public User() {

    }
}
