package com.gsx.mapper;

import com.gsx.vo.VideoVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收藏表 user_favorite 的增删查，结构与 UserLikeMapper 完全一样。
 * 唯一区别只是表名不同（收藏走 video_info.collect_count 计数）。
 */
@Mapper
@Repository
public interface UserFavoriteMapper {

    @Insert("""
            INSERT INTO easylive.user_favorite (user_id, video_id)
            VALUES (#{userId}, #{videoId})
            """)
    int insert(@Param("userId") String userId, @Param("videoId") Long videoId);

    @Delete("""
            DELETE FROM easylive.user_favorite
            WHERE user_id = #{userId} AND video_id = #{videoId}
            """)
    int deleteByUserAndVideo(@Param("userId") String userId, @Param("videoId") Long videoId);

    @Select("""
            SELECT COUNT(*) FROM easylive.user_favorite
            WHERE user_id = #{userId} AND video_id = #{videoId}
            """)
    int countByUserAndVideo(@Param("userId") String userId, @Param("videoId") Long videoId);

    @Select("""
            SELECT v.*, u.nick_name AS up_name, u.avatar AS up_avatar
            FROM easylive.user_favorite l
            JOIN easylive.video_info v ON l.video_id = v.video_id
            LEFT JOIN easylive.user_info u ON v.user_id = u.user_id
            WHERE l.user_id = #{userId} AND v.status = 0
            ORDER BY l.create_time DESC
            """)
    List<VideoVO> selectCollectedVideos(@Param("userId") String userId);
}
