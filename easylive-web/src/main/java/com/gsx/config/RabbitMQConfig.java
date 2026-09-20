package com.gsx.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue emailCodeQueue() {
        return new Queue("emailCodeQueue",true);
    }

    @Bean
    public DirectExchange Exchange1() {
        return new DirectExchange("Exchange1",true,false);
    }

    @Bean
    public Binding bindingEmailCode() {
        return BindingBuilder
                .bind(emailCodeQueue())
                .to(Exchange1())
                .with("emailCode");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter("com.gsx.handler.RabbitMQHandler");
        return converter;
    }
}
