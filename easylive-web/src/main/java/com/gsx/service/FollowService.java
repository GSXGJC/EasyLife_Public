package com.gsx.service;

import com.gsx.vo.FollowUserVO;

import java.util.List;

/**
 * 关注关系业务：关注/取关/我关注的名单/是否已关注。
 * 当前登录用户从 UserHolder 取（接口方法都不传 userId）。
 */
public interface FollowService {

    /**
     * 关注某人（幂等：已关注过也会返回 true，不会重复插）。
     * 抛业务异常的边界：关注自己、被关注者不存在。
     */
    boolean follow(String followedUserId);

    /**
     * 取消关注（没关注过也当成功，返回 false 表示现在是"未关注"态）。
     */
    boolean unfollow(String followedUserId);

    /**
     * 我关注的用户名单（私信面板联系人，按关注时间倒序）。
     */
    List<FollowUserVO> listFollows();

    /**
     * 当前用户是否已关注某人（详情页"已关注"按钮状态）。
     */
    boolean isFollowing(String followedUserId);
}
