package com.gsx.handler.RabbitMQ;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 通用发送方法（所有业务都用它）
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息体（可以是任何对象，Spring自动转JSON）
     */
    public void send(String exchange, String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            // 这里可以加统一的日志打印
        } catch (Exception e) {
            // 这里可以加统一的重试或异常告警
            throw new RuntimeException("MQ发送失败", e);
        }
    }
}