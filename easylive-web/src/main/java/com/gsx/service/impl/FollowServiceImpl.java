package com.gsx.service.impl;

import com.gsx.Utils.ResultUtil.ResultCode;
import com.gsx.handler.ExceptionHandler.ServiceException;
import com.gsx.handler.UserHolder;
import com.gsx.mapper.FollowMapper;
import com.gsx.mapper.UserMapper;
import com.gsx.service.FollowService;
import com.gsx.vo.FollowUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 关注业务实现。
 * 注意点：关注是"单向"的，不要求对方回关；本表只表达"谁关注了谁"。
 * 私信门槛（发送者须已关注接收者）由 ChatController 直接查 FollowMapper，这里不复用。
 */
@Service
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    FollowServiceImpl(@Autowired FollowMapper followMapper,
                      @Autowired UserMapper userMapper) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
    }

    /** 当前登录用户，未登录直接抛 401（沿用点赞那套 requireLogin 风格） */
    private String requireLogin() {
        String userId = UserHolder.getUserId();
        if (userId == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    @Override
    public boolean follow(String followedUserId) {
        String userId = requireLogin();
        // 两个边界：关注自己没意义；关注一个不存在的 userId 会造脏数据
        if (userId.equals(followedUserId)) {
            throw new ServiceException("不能关注自己");
        }
        if (userMapper.countByUserId(followedUserId) == 0) {
            throw new ServiceException("用户不存在");
        }
        // INSERT IGNORE：重复关注被唯一键挡掉，吸收成幂等，不影响结果
        followMapper.insertIgnore(userId, followedUserId);
        return true;
    }

    @Override
    public boolean unfollow(String followedUserId) {
        String userId = requireLogin();
        // 没关注过也是"未关注"结果，返回 0 不当异常
        followMapper.delete(userId, followedUserId);
        return false;
    }

    @Override
    public List<FollowUserVO> listFollows() {
        String userId = requireLogin();
        return followMapper.selectFollowList(userId);
    }

    @Override
    public boolean isFollowing(String followedUserId) {
        String userId = requireLogin();
        return followMapper.countFollow(userId, followedUserId) > 0;
    }
}
