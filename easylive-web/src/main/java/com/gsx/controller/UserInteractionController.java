package com.gsx.controller;

import com.gsx.Utils.ResultUtil.Result;
import com.gsx.service.UserInteractionService;
import com.gsx.vo.VideoInteractVO;
import com.gsx.vo.VideoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 互动接口：点赞/收藏/投币 + 互动状态 + 我的三个列表。
 *
 * 登录规则（由 MyInterceptor 决定）：
 * - POST /video/like 等写操作 → 必须登录（不在 GET /video 白名单里）
 * - GET /video/interact/{id} → 公开，但带 token 时能解析出"我点没点过"
 * - GET /video/liked/list 等"我的"列表 → 需要登录，没登录 service 层抛 401
 */
@RestController
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    UserInteractionController(@Autowired UserInteractionService userInteractionService) {
        this.userInteractionService = userInteractionService;
    }

    // ------------------------------------------------------------------
    // 写操作（都要登录）
    // ------------------------------------------------------------------

    @PostMapping("/video/like/{videoId}")
    public Result<VideoInteractVO> like(@PathVariable Long videoId) {
        return Result.success(userInteractionService.like(videoId));
    }

    @PostMapping("/video/unlike/{videoId}")
    public Result<VideoInteractVO> unlike(@PathVariable Long videoId) {
        return Result.success(userInteractionService.unlike(videoId));
    }

    @PostMapping("/video/favorite/{videoId}")
    public Result<VideoInteractVO> favorite(@PathVariable Long videoId) {
        return Result.success(userInteractionService.favorite(videoId));
    }

    @PostMapping("/video/unfavorite/{videoId}")
    public Result<VideoInteractVO> unfavorite(@PathVariable Long videoId) {
        return Result.success(userInteractionService.unfavorite(videoId));
    }

    /**
     * 投币。数量走 query 参数（?coinCount=2），默认 1。
     * 用 query 而不用 path，是因为 {videoId} 已经占了路径这一段，再多一段会和
     * GET /video/{videoId} 冲突。
     */
    @PostMapping("/video/coin/{videoId}")
    public Result<VideoInteractVO> coin(@PathVariable Long videoId,
                                        @RequestParam(defaultValue = "1") Integer coinCount) {
        return Result.success(userInteractionService.coin(videoId, coinCount));
    }

    // ------------------------------------------------------------------
    // 读：互动状态（公开，可选登录）
    // ------------------------------------------------------------------

    @GetMapping("/video/interact/{videoId}")
    public Result<VideoInteractVO> interact(@PathVariable Long videoId) {
        return Result.success(userInteractionService.getVideoInteract(videoId));
    }

    // ------------------------------------------------------------------
    // 读：我的列表（"点赞过的/收藏过的/投过币的"三个 tab）
    // ------------------------------------------------------------------

    @GetMapping("/video/liked/list")
    public Result<List<VideoVO>> likedList() {
        return Result.success(userInteractionService.getLikedVideos());
    }

    @GetMapping("/video/favorite/list")
    public Result<List<VideoVO>> favoriteList() {
        return Result.success(userInteractionService.getCollectedVideos());
    }

    @GetMapping("/video/coined/list")
    public Result<List<VideoVO>> coinedList() {
        return Result.success(userInteractionService.getCoinedVideos());
    }
}
