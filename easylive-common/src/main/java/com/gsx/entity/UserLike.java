package com.gsx.entity;

import java.time.LocalDateTime;

/**
 * 点赞实体，对应数据库表 user_like
 */
public class UserLike {

    /** 主键（自增） */
    private Long id;

    /** 点赞用户id，对应 user_info.user_id */
    private String userId;

    /** 被点赞视频id，对应 video_info.video_id */
    private Long videoId;

    /** 点赞时间 */
    private LocalDateTime createTime;

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

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
