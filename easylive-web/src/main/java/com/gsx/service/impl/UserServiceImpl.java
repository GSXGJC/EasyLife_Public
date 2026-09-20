package com.gsx.service.impl;

import com.gsx.Utils.JwtUtil;
import com.gsx.Utils.RedisUtil;
import com.gsx.Utils.ResultUtil.ResultCode;
import com.gsx.dto.LoginDto;
import com.gsx.dto.RegDto;
import com.gsx.entity.User;
import com.gsx.handler.ExceptionHandler.ServiceException;
import com.gsx.handler.UserHolder;
import com.gsx.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.gsx.service.UserService;
import com.gsx.vo.LoginVo;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.Random;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final RedisUtil redisUtil;
    UserMapper userMapper;
    JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
    UserServiceImpl(@Autowired UserMapper userMapper , @Autowired JwtUtil jwtUtil, RedisUtil redisUtil){
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.redisUtil = redisUtil;
    }

    @Override
    public LoginVo login(LoginDto loginDto) {
        User user = userMapper.login(loginDto);
        if(user == null|| !bcryptPasswordEncoder.matches(loginDto.getPassword(),user.getPassword())){
            throw new ServiceException(ResultCode.PASSWORD_ERROR);
        }
        String jti = UUID.randomUUID().toString();
        String jwt = jwtUtil.generateToken(user.getUserId(),user.getNickName(),jti);
        redisUtil.set(user.getUserId(),jti,60*60);
        LoginVo loginVo = new LoginVo(user.getNickName(),user.getUserId(),jwt);
        log.info(loginVo.toString());
        return loginVo;
    }

    public void logout(){
        redisUtil.del(UserHolder.getUserId());
    }

    @Override
    public void register(RegDto regDto) {
        String Email = regDto.getEmail();
        String storedCode = (String) redisUtil.get(Email);
        if (storedCode == null || !storedCode.equals(regDto.getVertifiCode())) {
            throw new ServiceException(ResultCode.EMAIL_CODE_WRONG);
        }
        redisUtil.del(Email);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        // ② StringBuilder：用来拼字符。循环里用 + 拼字符串会反复创建新对象，浪费
        StringBuilder sb = new StringBuilder(10);
        // ③ 随机数生成器
        Random random = new Random();
        // ④ 循环 10 次，每次从字符池随机挑一个字符
        for (int i = 0; i < 10; i++) {
            // nextInt(62) 生成 0~61 的随机整数，正好当字符串下标
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        // ⑤ 转成字符串，就是最终的用户 id
        String userId = sb.toString();
        String hashPassword = bcryptPasswordEncoder.encode(regDto.getPassword());
        User user = new User();
        user.setEmail(Email);
        user.setNickName(regDto.getNickName());
        user.setPassword(hashPassword);
        user.setUserId(userId);
        try {
            userMapper.insertUser(user);
        } catch (DuplicateKeyException e) {
            if (e.getMessage().contains("idx_key_email")) {
                throw new ServiceException(ResultCode.EMAIL_USED);
            }
        }
    }
}
