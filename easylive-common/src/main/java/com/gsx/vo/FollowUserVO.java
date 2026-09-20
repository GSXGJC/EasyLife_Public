package com.gsx.vo;

import java.time.LocalDateTime;

/**
 * 我关注的用户返回体（私信名单里的对象）。
 * 昵称/头像来自 JOIN user_info，关系时间来自 user_follow。
 */
public class FollowUserVO {

    /** 被关注者 userId（即私信对象） */
    private String userId;

    /** 昵称（JOIN user_info.nick_name） */
    private String nickName;

    /** 头像（JOIN user_info.avatar） */
    private String avatar;

    /** 关注时间（user_follow.create_time，按此倒序排名单） */
    private LocalDateTime createTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
