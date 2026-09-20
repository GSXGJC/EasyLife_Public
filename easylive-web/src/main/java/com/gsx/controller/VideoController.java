package com.gsx.controller;

import com.gsx.Utils.ResultUtil.Result;
import com.gsx.service.impl.VideoServiceImpl;
import com.gsx.vo.VideoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class VideoController {

    private final VideoServiceImpl videoServiceImpl;

    VideoController(@Autowired VideoServiceImpl videoServiceImpl) {
        this.videoServiceImpl = videoServiceImpl;
    }

    /**
     * 发布视频（multipart/form-data），需登录。
     * 拦截器只放行 GET 的 /video/**，POST /video/publish 必须带有效 token。
     */
    @PostMapping("/video/publish")
    public Result publish(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) Integer category,
            @RequestParam(value = "duration", required = false) Integer duration) {
        videoServiceImpl.publish(file, cover, title, description, category, duration);
        return Result.success();
    }

    /**
     * 视频列表（公开，分页）。
     */
    @GetMapping("/video/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer pageSize) {
        return Result.success(videoServiceImpl.getVideoList(page, pageSize));
    }

    /**
     * 视频详情（公开）。
     */
    @GetMapping("/video/{videoId}")
    public Result<VideoVO> detail(@PathVariable Long videoId) {
        return Result.success(videoServiceImpl.getVideoDetail(videoId));
    }

    /**
     * 我的投稿（当前登录用户发布的视频）。
     * 拦截器对 GET /video/** 是"可选登录"，没登录时 service 层会抛 401。
     * 路径用 /video/my/list 两段，避免和 /video/{videoId} 单段模板冲突。
     */
    @GetMapping("/video/my/list")
    public Result<List<VideoVO>> myList() {
        return Result.success(videoServiceImpl.getMyVideos());
    }
}
