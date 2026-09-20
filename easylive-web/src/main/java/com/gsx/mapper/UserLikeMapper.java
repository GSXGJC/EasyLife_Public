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
 * 点赞表 user_like 的增删查。
 * 无 status 列、不加物理外键（延续项目风格），取消点赞就是 DELETE 这一行。
 * insert/delete 返回 int = 影响的行数：0 表示"没插进去/没删到"，service 用它判断重复。
 */
@Mapper
@Repository
public interface UserLikeMapper {

    /**
     * 点赞：插一行 (user_id, video_id)。
     * 表上有 UNIQUE KEY uk_user_video，同一人重复赞同一视频会插不进去（抛重复键异常）。
     */
    @Insert("""
            INSERT INTO easylive.user_like (user_id, video_id)
            VALUES (#{userId}, #{videoId})
            """)
    int insert(@Param("userId") String userId, @Param("videoId") Long videoId);

    /**
     * 取消点赞：删掉这一行。返回 0 = 本来就没点过赞。
     */
    @Delete("""
            DELETE FROM easylive.user_like
            WHERE user_id = #{userId} AND video_id = #{videoId}
            """)
    int deleteByUserAndVideo(@Param("userId") String userId, @Param("videoId") Long videoId);

    /**
     * 当前用户是否已点赞这个视频（count > 0 就是点过了）。
     */
    @Select("""
            SELECT COUNT(*) FROM easylive.user_like
            WHERE user_id = #{userId} AND video_id = #{videoId}
            """)
    int countByUserAndVideo(@Param("userId") String userId, @Param("videoId") Long videoId);

    /**
     * 「我点赞过的视频」列表。
     * 从关系表 l 出发 JOIN video_info v，再 LEFT JOIN user_info 拿作者信息，
     * 返回结构复用 VideoVO（和视频列表/详情页同一个返回体）。
     * 按 l.create_time 倒序 = 最近点赞的排前面。
     */
    @Select("""
            SELECT v.*, u.nick_name AS up_name, u.avatar AS up_avatar
            FROM easylive.user_like l
            JOIN easylive.video_info v ON l.video_id = v.video_id
            LEFT JOIN easylive.user_info u ON v.user_id = u.user_id
            WHERE l.user_id = #{userId} AND v.status = 0
            ORDER BY l.create_time DESC
            """)
    List<VideoVO> selectLikedVideos(@Param("userId") String userId);
}
