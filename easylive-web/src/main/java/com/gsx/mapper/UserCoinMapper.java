package com.gsx.mapper;

import com.gsx.entity.UserCoin;
import com.gsx.vo.VideoVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 投币表 user_coin 的增查。
 * 注意：投币没有"取消"接口（bilibili 投币后不能撤回），所以这里没有 delete 方法。
 * 唯一键 uk_user_video 保证一个用户对一个视频最多投一次。
 */
@Mapper
@Repository
public interface UserCoinMapper {

    /**
     * 投币：插一行，记下投了几个币（1~2）。
     */
    @Insert("""
            INSERT INTO easylive.user_coin (user_id, video_id, coin_count)
            VALUES (#{userId}, #{videoId}, #{coinCount})
            """)
    int insert(@Param("userId") String userId,
               @Param("videoId") Long videoId,
               @Param("coinCount") Integer coinCount);

    /**
     * 查某用户对某视频的投币记录。
     * 返回 null = 没投过；返回对象则能从 coinCount 得知投了几个币。
     */
    @Select("""
            SELECT * FROM easylive.user_coin
            WHERE user_id = #{userId} AND video_id = #{videoId}
            """)
    UserCoin selectByUserAndVideo(@Param("userId") String userId, @Param("videoId") Long videoId);

    @Select("""
            SELECT v.*, u.nick_name AS up_name, u.avatar AS up_avatar
            FROM easylive.user_coin l
            JOIN easylive.video_info v ON l.video_id = v.video_id
            LEFT JOIN easylive.user_info u ON v.user_id = u.user_id
            WHERE l.user_id = #{userId} AND v.status = 0
            ORDER BY l.create_time DESC
            """)
    List<VideoVO> selectCoinedVideos(@Param("userId") String userId);
}
