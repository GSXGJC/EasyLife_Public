package com.gsx.service.impl;

import com.gsx.Utils.RedisUtil;
import com.gsx.Utils.ResultUtil.Result;
import com.gsx.Utils.ResultUtil.ResultCode;
import com.gsx.handler.ExceptionHandler.ServiceException;
import com.gsx.handler.RabbitMQ.RabbitMQProducer;
import com.gsx.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {
    RedisUtil redisUtil;
    private final RabbitMQProducer mqProducer;

    public EmailServiceImpl(@Autowired RedisUtil redisUtil,
                            @Autowired RabbitMQProducer mqProducer) {
        this.redisUtil = redisUtil;
        this.mqProducer = mqProducer;
    }

    private static final String EXCHANGE = "Exchange1";
    private static final String ROUTING_KEY = "emailCode";

    @Override
    public void sendEmail(String email) {
        if(redisUtil.get(email)!=null) throw new ServiceException(ResultCode.EMAIL_CODE_SENT);
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 100000-999999
        String verificationCode = String.valueOf(code);
        // 发送验证码给RabbitMQ
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("code", verificationCode);
        // 调用工具类发送
        mqProducer.send(EXCHANGE, ROUTING_KEY, payload);
        redisUtil.set(email, verificationCode, 3*60);
    }
}
