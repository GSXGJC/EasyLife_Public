package com.gsx.vo;

import java.time.LocalDateTime;

/**
 * 视频 + 作者信息的返回体。
 * 作者昵称/头像来自 JOIN user_info，不冗余在 video_info 表里。
 * 放在 web 模块（不用重装 easylive-common 那个坑）。
 */
public class VideoVO {

    private Long videoId;
    private String userId;
    private String title;
    private String description;
    private Integer category;
    private String cover;
    private String videoUrl;
    private Integer duration;
    private Integer playCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer coinCount;
    private Integer collectCount;
    private LocalDateTime createTime;

    /** up 主昵称（JOIN user_info.nick_name） */
    private String upName;
    /** up 主头像（JOIN user_info.avatar） */
    private String upAvatar;

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCategory() { return category; }
    public void setCategory(Integer category) { this.category = category; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getPlayCount() { return playCount; }
    public void setPlayCount(Integer playCount) { this.playCount = playCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public Integer getCoinCount() { return coinCount; }
    public void setCoinCount(Integer coinCount) { this.coinCount = coinCount; }

    public Integer getCollectCount() { return collectCount; }
    public void setCollectCount(Integer collectCount) { this.collectCount = collectCount; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getUpName() { return upName; }
    public void setUpName(String upName) { this.upName = upName; }

    public String getUpAvatar() { return upAvatar; }
    public void setUpAvatar(String upAvatar) { this.upAvatar = upAvatar; }
}
