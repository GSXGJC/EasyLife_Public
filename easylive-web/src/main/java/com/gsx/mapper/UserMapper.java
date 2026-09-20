package com.gsx.mapper;

import com.gsx.dto.LoginDto;
import com.gsx.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface UserMapper {
    @Select("SELECT * from easylive.user_info where email = #{email} LIMIT 1")
    User login(LoginDto loginDto);

    @Insert("""
            INSERT INTO easylive.user_info (
                user_id,                -- 主键 varchar(10)，由 service 生成（注意最长 10 个字符）
                nick_name,              -- 昵称 varchar(20)，必填
                email,
                password               -- 密码 varchar(50)，必填
            ) VALUES (
                #{userId},              -- 取 User.getUserId()
                #{nickName},            -- 取 User.getNickName()
                #{email},         -- 取 User.getEmail()
                #{password}            -- 取 User.getPassword()
            )
            """)
    void insertUser(User user);

    /**
     * 按 userId 判断用户是否存在（关注前校验被关注者，防造出脏数据）。
     */
    @Select("SELECT COUNT(*) FROM easylive.user_info WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") String userId);

    // ------------------------------------------------------------------
    // 硬币相关（投币时由 UserInteractionService 调用）
    // ------------------------------------------------------------------

    /**
     * 查当前硬币余额。返回 null 表示查不到这个用户（理论上不会发生，因为能走到这里说明已登录）。
     */
    @Select("SELECT current_coin_count FROM easylive.user_info WHERE user_id = #{userId}")
    Integer selectCurrentCoinCount(@Param("userId") String userId);

    /**
     * 扣硬币，带余额条件：只有 current_coin_count >= coinCount 才扣成功。
     * 返回 0 = 余额不够（WHERE 条件不满足，什么都没改）。
     * 用"条件更新"而不是"先查再扣"，能在并发下也保证余额不会被扣成负数。
     */
    @Update("""
            UPDATE easylive.user_info
            SET current_coin_count = current_coin_count - #{coinCount}
            WHERE user_id = #{userId} AND current_coin_count >= #{coinCount}
            """)
    int deductCoin(@Param("userId") String userId, @Param("coinCount") Integer coinCount);

}
