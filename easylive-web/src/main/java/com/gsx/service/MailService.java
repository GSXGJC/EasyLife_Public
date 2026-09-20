package com.gsx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    // 从配置文件中读取发件人
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送纯文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            System.out.println("邮件发送成功: " + to);
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
            // 生产环境这里建议抛出自定义异常或记录错误日志
        }
    }
}