package com.gsx.mapper;

import com.gsx.vo.FollowUserVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 关注表 user_follow 的增删查。
 * 一条记录 = userId 关注了 followedUserId（单向）。
 * 私信门槛复用这里的 countFollow：发送者必须已关注接收者才允许发。
 */
@Mapper
@Repository
public interface FollowMapper {

    /**
     * 关注：插一行 (user_id, followed_user_id)。
     * 表上有 UNIQUE KEY uk_user_followed，同一人重复关注会被挡。
     * 用 INSERT IGNORE：撞唯一键时静默跳过，把"重复关注"吸收成幂等，不抛异常。
     * 返回 1 = 新关注成功，0 = 本来就关注着（被 ignore 掉）。
     */
    @Insert("""
            INSERT IGNORE INTO easylive.user_follow (user_id, followed_user_id)
            VALUES (#{userId}, #{followedUserId})
            """)
    int insertIgnore(@Param("userId") String userId, @Param("followedUserId") String followedUserId);

    /**
     * 取消关注：删掉这一行。返回 0 = 本来就没关注。
     */
    @Delete("""
            DELETE FROM easylive.user_follow
            WHERE user_id = #{userId} AND followed_user_id = #{followedUserId}
            """)
    int delete(@Param("userId") String userId, @Param("followedUserId") String followedUserId);

    /**
     * 是否已关注：count > 0 = 关注了。私信门槛也走这里。
     */
    @Select("""
            SELECT COUNT(*) FROM easylive.user_follow
            WHERE user_id = #{userId} AND followed_user_id = #{followedUserId}
            """)
    int countFollow(@Param("userId") String userId, @Param("followedUserId") String followedUserId);

    /**
     * 「我关注的用户」名单（私信面板的联系人）。
     * 从关系表 f 出发 JOIN user_info 拿昵称/头像，按关注时间倒序（最近关注的排前面）。
     */
    @Select("""
            SELECT u.user_id AS userId, u.nick_name AS nickName,
                   u.avatar AS avatar, f.create_time AS createTime
            FROM easylive.user_follow f
            JOIN easylive.user_info u ON f.followed_user_id = u.user_id
            WHERE f.user_id = #{userId}
            ORDER BY f.create_time DESC
            """)
    List<FollowUserVO> selectFollowList(@Param("userId") String userId);
}
