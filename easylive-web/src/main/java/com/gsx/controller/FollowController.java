package com.gsx.controller;

import com.gsx.Utils.ResultUtil.Result;
import com.gsx.service.FollowService;
import com.gsx.vo.FollowUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 关注接口：关注/取关 + 我关注的名单 + 是否已关注。
 *
 * 登录规则：全在 /user 下，不在 MyInterceptor 的 GET /video 白名单里，
 * 所以这些接口都必须登录（正好，关注/私信名单本来就需要登录态）。
 * 未登录会被 MyInterceptor 拦成 401。
 */
@RestController
public class FollowController {

    private final FollowService followService;

    FollowController(@Autowired FollowService followService) {
        this.followService = followService;
    }

    /** 关注（幂等：已关注过也返回 true，不会重复）。data = true 表示现在是"已关注" */
    @PostMapping("/user/follow/{followedUserId}")
    public Result<Boolean> follow(@PathVariable String followedUserId) {
        return Result.success(followService.follow(followedUserId));
    }

    /** 取消关注。data = false 表示现在是"未关注" */
    @DeleteMapping("/user/follow/{followedUserId}")
    public Result<Boolean> unfollow(@PathVariable String followedUserId) {
        return Result.success(followService.unfollow(followedUserId));
    }

    /** 我关注的用户名单（私信面板联系人） */
    @GetMapping("/user/follow/list")
    public Result<List<FollowUserVO>> followList() {
        return Result.success(followService.listFollows());
    }

    /** 当前用户是否已关注某人（详情页按钮状态）。用 /status/{id} 避免和 /list 混淆 */
    @GetMapping("/user/follow/status/{followedUserId}")
    public Result<Boolean> followStatus(@PathVariable String followedUserId) {
        return Result.success(followService.isFollowing(followedUserId));
    }
}
