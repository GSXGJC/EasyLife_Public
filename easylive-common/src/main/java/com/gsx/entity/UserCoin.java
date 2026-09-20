package com.gsx.entity;

import java.time.LocalDateTime;

/**
 * 投币实体，对应数据库表 user_coin
 */
public class UserCoin {

    /** 主键（自增） */
    private Long id;

    /** 投币用户id，对应 user_info.user_id */
    private String userId;

    /** 被投币视频id，对应 video_info.video_id */
    private Long videoId;

    /** 投币数量：1~2 */
    private Integer coinCount;

    /** 投币时间 */
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

    public Integer getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(Integer coinCount) {
        this.coinCount = coinCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
