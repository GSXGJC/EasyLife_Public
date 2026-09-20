package com.gsx.service.impl;

import com.gsx.Utils.ResultUtil.ResultCode;
import com.gsx.entity.UserCoin;
import com.gsx.handler.ExceptionHandler.ServiceException;
import com.gsx.handler.UserHolder;
import com.gsx.mapper.UserCoinMapper;
import com.gsx.mapper.UserFavoriteMapper;
import com.gsx.mapper.UserLikeMapper;
import com.gsx.mapper.UserMapper;
import com.gsx.mapper.VideoMapper;
import com.gsx.service.UserInteractionService;
import com.gsx.vo.VideoInteractVO;
import com.gsx.vo.VideoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 点赞/收藏/投币的核心逻辑。
 *
 * 每个动作都是"两/三步跨表写"，所以加 @Transactional：中途任何一步抛异常，
 * 前面已经执行的 SQL 一起回滚。比如投币：先扣了用户硬币，结果插 user_coin 失败，
 * 如果没事务，用户币就白扣了。
 */
@Service
public class UserInteractionServiceImpl implements UserInteractionService {

    private final UserLikeMapper userLikeMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserCoinMapper userCoinMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;

    UserInteractionServiceImpl(@Autowired UserLikeMapper userLikeMapper,
                               @Autowired UserFavoriteMapper userFavoriteMapper,
                               @Autowired UserCoinMapper userCoinMapper,
                               @Autowired VideoMapper videoMapper,
                               @Autowired UserMapper userMapper) {
        this.userLikeMapper = userLikeMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userCoinMapper = userCoinMapper;
        this.videoMapper = videoMapper;
        this.userMapper = userMapper;
    }

    // ------------------------------------------------------------------
    // 读：互动状态
    // ------------------------------------------------------------------

    @Override
    public VideoInteractVO getVideoInteract(Long videoId) {
        // 匿名（没登录）时 UserHolder.getUserId() 是 null，后面 buildVO 里统一按"全没点"处理
        String userId = UserHolder.getUserId();
        return buildVO(videoId, userId);
    }

    // ------------------------------------------------------------------
    // 写：点赞
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public VideoInteractVO like(Long videoId) {
        String userId = requireLogin();
        if (userLikeMapper.countByUserAndVideo(userId, videoId) > 0) {
            throw new ServiceException("你已点过赞了");
        }
        // 先插关系表：说明"谁赞了哪个视频"
        userLikeMapper.insert(userId, videoId);
        // 再改冗余计数：video_info.like_count + 1
        videoMapper.increaseLikeCount(videoId);
        return buildVO(videoId, userId);
    }

    @Override
    @Transactional
    public VideoInteractVO unlike(Long videoId) {
        String userId = requireLogin();
        // delete 返回受影响行数：0 说明关系表里根本没这行 = 没点过赞
        int deleted = userLikeMapper.deleteByUserAndVideo(userId, videoId);
        if (deleted == 0) {
            throw new ServiceException("你还未点赞，无法取消");
        }
        videoMapper.decreaseLikeCount(videoId);
        return buildVO(videoId, userId);
    }

    // ------------------------------------------------------------------
    // 写：收藏
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public VideoInteractVO favorite(Long videoId) {
        String userId = requireLogin();
        if (userFavoriteMapper.countByUserAndVideo(userId, videoId) > 0) {
            throw new ServiceException("你已经收藏过了");
        }
        userFavoriteMapper.insert(userId, videoId);
        videoMapper.increaseCollectCount(videoId);
        return buildVO(videoId, userId);
    }

    @Override
    @Transactional
    public VideoInteractVO unfavorite(Long videoId) {
        String userId = requireLogin();
        int deleted = userFavoriteMapper.deleteByUserAndVideo(userId, videoId);
        if (deleted == 0) {
            throw new ServiceException("你还未收藏，无法取消");
        }
        videoMapper.decreaseCollectCount(videoId);
        return buildVO(videoId, userId);
    }

    // ------------------------------------------------------------------
    // 写：投币（最复杂：扣用户币 + 插投币记录 + 加视频币数，三步跨三张表）
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public VideoInteractVO coin(Long videoId, Integer coinCount) {
        String userId = requireLogin();
        // 1. 数量只允许 1 或 2（表结构是 TINYINT，前端给默认 1）
        if (coinCount == null || coinCount < 1 || coinCount > 2) {
            throw new ServiceException("投币数量只能为 1 或 2");
        }
        // 2. 一个视频只能投一次（uk_user_video 唯一键兜底，这里提前拦更友好）
        if (userCoinMapper.selectByUserAndVideo(userId, videoId) != null) {
            throw new ServiceException("你已给这个视频投过币了");
        }
        // 3. 扣硬币。条件更新 current_coin_count >= coinCount 才扣得动，返回 0 就是余额不够
        int deducted = userMapper.deductCoin(userId, coinCount);
        if (deducted == 0) {
            throw new ServiceException("硬币不足，无法投币");
        }
        // 4. 插投币记录（记录"谁给哪个视频投了几个"）
        userCoinMapper.insert(userId, videoId, coinCount);
        // 5. 视频计数 +coinCount（投 2 个就 +2）
        videoMapper.increaseCoinCount(videoId, coinCount);
        return buildVO(videoId, userId);
    }

    // ------------------------------------------------------------------
    // 读：我的列表（这三个 tab 是"点赞/收藏/投币"功能的读侧）
    // ------------------------------------------------------------------

    @Override
    public List<VideoVO> getLikedVideos() {
        return userLikeMapper.selectLikedVideos(requireLogin());
    }

    @Override
    public List<VideoVO> getCollectedVideos() {
        return userFavoriteMapper.selectCollectedVideos(requireLogin());
    }

    @Override
    public List<VideoVO> getCoinedVideos() {
        return userCoinMapper.selectCoinedVideos(requireLogin());
    }

    // ------------------------------------------------------------------
    // 私有工具
    // ------------------------------------------------------------------

    /**
     * 取当前登录用户，没登录直接抛 401。
     * 详情/列表的公开 GET 能匿名访问；但"点赞/取消/投币"这些写操作必须登录。
     */
    private String requireLogin() {
        String userId = UserHolder.getUserId();
        if (userId == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 组装互动状态 VO。统一在操作完成后重新查一次 video_info（拿最新计数），
     * 保证返回的计数和数据库一致，前端不用猜。
     */
    private VideoInteractVO buildVO(Long videoId, String userId) {
        VideoVO video = videoMapper.selectVideoById(videoId);
        if (video == null) {
            throw new ServiceException("视频不存在");
        }

        VideoInteractVO vo = new VideoInteractVO();
        vo.setLikeCount(video.getLikeCount());
        vo.setCoinCount(video.getCoinCount());
        vo.setCollectCount(video.getCollectCount());

        // 没登录：按钮全是"未激活"，也没有硬币余额概念
        if (userId == null) {
            vo.setLiked(false);
            vo.setCollected(false);
            vo.setCoined(false);
            vo.setMyCoinCount(0);
            vo.setCurrentCoinCount(0);
            return vo;
        }

        vo.setLiked(userLikeMapper.countByUserAndVideo(userId, videoId) > 0);
        vo.setCollected(userFavoriteMapper.countByUserAndVideo(userId, videoId) > 0);

        UserCoin coin = userCoinMapper.selectByUserAndVideo(userId, videoId);
        vo.setCoined(coin != null);
        vo.setMyCoinCount(coin == null ? 0 : coin.getCoinCount());

        Integer balance = userMapper.selectCurrentCoinCount(userId);
        vo.setCurrentCoinCount(balance == null ? 0 : balance);
        return vo;
    }
}
