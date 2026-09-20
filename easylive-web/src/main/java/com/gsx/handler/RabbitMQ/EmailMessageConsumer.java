package com.gsx.handler.RabbitMQ;

import com.gsx.service.MailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailMessageConsumer {

    @Autowired
    private MailService mailService;

    @RabbitListener(queues = "emailCodeQueue")
    public void handleEmailCode(Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");

        // 组装邮件正文
        String subject = "easylive(●'◡'●)邮箱验证码";
        String content = "您正在注册/登录，验证码为：" + code + "，有效期5分钟，请勿泄露。";

        // 调用邮件模块发送
        mailService.sendSimpleMail(email, subject, content);
    }
}