package com.gsx.service.impl;

import com.gsx.Utils.ResultUtil.ResultCode;
import com.gsx.entity.Video;
import com.gsx.handler.ExceptionHandler.ServiceException;
import com.gsx.handler.UserHolder;
import com.gsx.mapper.VideoMapper;
import com.gsx.service.VideoService;
import com.gsx.vo.VideoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoServiceImpl.class);

    /**
     * 上传文件根目录，从配置 easylive.upload-path 读取，
     * 必须和 WebConfig 里 addResourceLocations 指向的是同一处。
     * 本地默认 Windows 路径；Linux 服务器部署时用 --easylive.upload-path 覆盖。
     */
    @Value("${easylive.upload-path:./upload}")
    private String uploadRoot;

    private final VideoMapper videoMapper;

    VideoServiceImpl(@Autowired VideoMapper videoMapper) {
        this.videoMapper = videoMapper;
    }

    @Override
    public void publish(MultipartFile file, MultipartFile cover, String title,
                        String description, Integer category, Integer duration) {
        // 1. 校验
        if (file == null || file.isEmpty()) {
            throw new ServiceException("视频文件不能为空");
        }
        if (title == null || title.isBlank()) {
            throw new ServiceException("标题不能为空");
        }

        // 2. 当前登录用户就是 up 主（拦截器保证能走到这里的一定已登录）
        String userId = UserHolder.getUserId();

        // 3. 存文件，拿到可被前端直接访问的 URL 路径
        String videoUrl = saveFile(file, uploadRoot + "/videos", "/api/upload/videos/");
        String coverUrl = null;
        if (cover != null && !cover.isEmpty()) {
            // 图片单独限 5MB（multipart 的 max-file-size 是"每个文件"统一限制，
            // 分不出视频还是图片，所以图片的单独限制在业务代码里做）
            if (cover.getSize() > 5 * 1024 * 1024) {
                throw new ServiceException("封面图片过大，请上传 5MB 以内的图片");
            }
            coverUrl = saveFile(cover, uploadRoot + "/covers", "/api/upload/covers/");
        }

        // 4. 组装实体入库；category/duration 没传时给 0，别写 NULL 进 NOT NULL 列
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description);
        video.setCategory(category == null ? 0 : category);
        video.setCover(coverUrl);
        video.setVideoUrl(videoUrl);
        video.setDuration(duration == null ? 0 : duration);
        videoMapper.insertVideo(video);
    }

    /**
     * 把 MultipartFile 落盘，返回存储的 URL 路径。
     * 文件名用 UUID 避免重名覆盖；保留原扩展名，播放器要靠它识别格式。
     */
    private String saveFile(MultipartFile file, String dir, String urlPrefix) {
        try {
            Files.createDirectories(Paths.get(dir));   // 目录不存在就建
            String original = file.getOriginalFilename();       // 例如 "demo.mp4"
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));  // ".mp4"
            }
            String filename = UUID.randomUUID() + ext;            // "2f9c...-8a1f.mp4"
            file.transferTo(new File(dir, filename));             // 真正写盘
            return urlPrefix + filename;                          // "/api/upload/videos/xxx.mp4"
        } catch (IOException e) {
            // 打印真实原因（权限/磁盘满/路径不对），方便服务器上排查
            log.error("文件保存失败, dir={}", dir, e);
            throw new ServiceException("文件保存失败");
        }
    }

    @Override
    public Map<String, Object> getVideoList(int page, int pageSize) {
        // 前端传的是"第几页"（从 1 开始），数据库 LIMIT 要的是"跳过几行"
        int offset = (page - 1) * pageSize;
        List<VideoVO> list = videoMapper.selectVideoList(offset, pageSize);
        long total = videoMapper.countVideo();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("hasMore", (long) page * pageSize < total);
        return result;
    }

    @Override
    public VideoVO getVideoDetail(Long videoId) {
        VideoVO video = videoMapper.selectVideoById(videoId);
        if (video == null) {
            throw new ServiceException("视频不存在");
        }
        videoMapper.increasePlayCount(videoId);
        return video;
    }

    @Override
    public List<VideoVO> getMyVideos() {
        // 拦截器对 GET /video/** 是"可选登录"，走到这必须真登录，
        // 没登录 UserHolder 里是 null，这里拦一道（和互动"我的列表"同一套路）
        String userId = UserHolder.getUserId();
        if (userId == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED);
        }

        return videoMapper.selectMyVideoList(userId);
    }
}
