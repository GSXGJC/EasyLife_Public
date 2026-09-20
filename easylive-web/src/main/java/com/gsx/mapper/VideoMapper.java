package com.gsx.mapper;

import com.gsx.entity.Video;
import com.gsx.vo.VideoVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface VideoMapper {

    /**
     * 插入视频记录。
     * 计数器/状态/发布时间都不写，靠 DB 默认值兜底；
     * video_id 是自增主键，不用管。
     */
    @Insert("""
            INSERT INTO  easylive.video_info (
                user_id, title, description, category,
                cover, video_url, duration
            ) VALUES (
                #{userId}, #{title}, #{description}, #{category},
                #{cover}, #{videoUrl}, #{duration}
            )
            """)
    void insertVideo(Video video);

    /**
     * 分页查视频列表，LEFT JOIN user_info 拿作者昵称/头像。
     * v.* 的列靠 map-underscore-to-camel-case 自动映射成 camelCase 字段。
     * LIMIT 前一个数是跳过的行数（offset），后一个数是取几条。
     */
    @Select("""
            SELECT v.*, u.nick_name AS up_name, u.avatar AS up_avatar
            FROM easylive.video_info v
            LEFT JOIN easylive.user_info u ON v.user_id = u.user_id
            WHERE v.status = 0
            ORDER BY v.create_time DESC
            LIMIT #{offset}, #{pageSize}
            """)
    List<VideoVO> selectVideoList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 查单个视频详情（同样 JOIN 作者信息）。
     */
    @Select("""
            SELECT v.*, u.nick_name AS up_name, u.avatar AS up_avatar
            FROM easylive.video_info v
            LEFT JOIN easylive.user_info u ON v.user_id = u.user_id
            WHERE v.video_id = #{videoId}
            """)
    VideoVO selectVideoById(@Param("videoId") Long videoId);

    /**
     * 总数，用于前端判断"还有没有下一页"。
     */
    @Select("SELECT COUNT(*) FROM easylive.video_info WHERE status = 0")
    long countVideo();

    /**
     * 查某个 up 主（当前登录用户）发布的视频，即「我的投稿」。
     * WHERE v.user_id = #{userId}：video_info.user_id 就是上传时记下的登录用户 id。
     */
    @Select("""
            SELECT v.*, u.nick_name AS up_name, u.avatar AS up_avatar
            FROM easylive.video_info v
            LEFT JOIN easylive.user_info u ON v.user_id = u.user_id
            WHERE v.user_id = #{userId} AND v.status = 0
            ORDER BY v.create_time DESC
            """)
    List<VideoVO> selectMyVideoList(@Param("userId") String userId);


    @Update("UPDATE easylive.video_info SET play_count = play_count + 1 WHERE video_id = #{videoId}")
    void increasePlayCount(@Param("videoId") Long videoId);
    // ------------------------------------------------------------------
    // 互动计数联动（点赞/收藏/投币时由 UserInteractionService 调用）
    // 设计：video_info 行内冗余计数器（bilibili 模型），互动时 +1/-1。
    // 减的时候用 IF(count > 0, count - 1, 0)：防止"取消一个没赞过的赞"
    // 或并发场景下把计数减成负数。
    // ------------------------------------------------------------------

    @Update("UPDATE easylive.video_info SET like_count = like_count + 1 WHERE video_id = #{videoId}")
    int increaseLikeCount(@Param("videoId") Long videoId);

    @Update("UPDATE easylive.video_info SET like_count = IF(like_count > 0, like_count - 1, 0) WHERE video_id = #{videoId}")
    int decreaseLikeCount(@Param("videoId") Long videoId);

    @Update("UPDATE easylive.video_info SET collect_count = collect_count + 1 WHERE video_id = #{videoId}")
    int increaseCollectCount(@Param("videoId") Long videoId);

    @Update("UPDATE easylive.video_info SET collect_count = IF(collect_count > 0, collect_count - 1, 0) WHERE video_id = #{videoId}")
    int decreaseCollectCount(@Param("videoId") Long videoId);

    @Update("UPDATE easylive.video_info SET coin_count = coin_count + #{coinCount} WHERE video_id = #{videoId}")
    int increaseCoinCount(@Param("videoId") Long videoId, @Param("coinCount") Integer coinCount);
}
