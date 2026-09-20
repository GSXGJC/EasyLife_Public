package com.gsx.service;

import com.gsx.vo.VideoInteractVO;
import com.gsx.vo.VideoVO;

import java.util.List;

/**
 * 用户对视频的互动：点赞/收藏/投币。
 * 每个"写"接口都返回最新 VideoInteractVO，前端拿它刷按钮状态和计数。
 */
public interface UserInteractionService {

    /**
     * 查当前用户对该视频的互动状态（登录了看真实状态，没登录全是 false）。
     */
    VideoInteractVO getVideoInteract(Long videoId);

    /**
     * 点赞。已点过则抛业务异常。
     */
    VideoInteractVO like(Long videoId);

    /**
     * 取消点赞。没点过则抛业务异常。
     */
    VideoInteractVO unlike(Long videoId);

    /**
     * 收藏。
     */
    VideoInteractVO favorite(Long videoId);

    /**
     * 取消收藏。
     */
    VideoInteractVO unfavorite(Long videoId);

    /**
     * 投币（1~2 个）。已投过/余额不足都抛业务异常。
     */
    VideoInteractVO coin(Long videoId, Integer coinCount);

    /**
     * 我点赞过的视频列表（详情倒序）。
     */
    List<VideoVO> getLikedVideos();

    /**
     * 我收藏过的视频列表。
     */
    List<VideoVO> getCollectedVideos();

    /**
     * 我投过币的视频列表。
     */
    List<VideoVO> getCoinedVideos();
}
