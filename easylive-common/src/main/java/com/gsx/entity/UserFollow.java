package com.gsx.entity;

import java.time.LocalDateTime;

/**
 * 关注关系实体，对应数据库表 user_follow
 * 语义：userId 关注了 followedUserId（单向，关注不要求对方回关）
 */
public class UserFollow {

    /** 主键（自增） */
    private Long id;

    /** 关注者id（谁点的关注），对应 user_info.user_id */
    private String userId;

    /** 被关注者id（up主），对应 user_info.user_id */
    private String followedUserId;

    /** 关注时间 */
    private LocalDateTime createTime;

    public int getMessageToRead() {
        return messageToRead;
    }

    public void setMessageToRead(int messageToRead) {
        this.messageToRead = messageToRead;
    }

    private int messageToRead;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFollowedUserId() {
        return followedUserId;
    }

    public void setFollowedUserId(String followedUserId) {
        this.followedUserId = followedUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
