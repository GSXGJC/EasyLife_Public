package com.gsx.service;

import com.gsx.vo.VideoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface VideoService {

    /**
     * 发布视频：存文件 + 写 video_info 记录。
     */
    void publish(MultipartFile file, MultipartFile cover, String title,
                 String description, Integer category, Integer duration);

    /**
     * 分页查视频列表，返回 { list, total, hasMore }。
     */
    Map<String, Object> getVideoList(int page, int pageSize);

    /**
     * 查视频详情，不存在抛 ServiceException("视频不存在")。
     */
    VideoVO getVideoDetail(Long videoId);

    /**
     * 查当前登录用户发布的视频（「我的投稿」），没登录抛 401。
     * 核心表是 video_info（按 user_id 过滤），所以归 VideoService。
     */
    List<VideoVO> getMyVideos();
}
