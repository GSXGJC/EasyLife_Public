package com.gsx.config;

import com.gsx.security.UserPrincipal;
import com.gsx.handler.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    WebSocketInterceptor webSocketInterceptor;
    WebSocketConfig(@Autowired WebSocketInterceptor webSocketInterceptor){
        this.webSocketInterceptor = webSocketInterceptor;
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(webSocketInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        // 从拦截器塞入的 attributes 中取出 userId 和昵称
                        String userId = (String) attributes.get("userId");
                        if (userId != null) {
                            // 包装成 Principal，Spring 会自动绑定到当前 WebSocket 会话
                            String nickName = (String) attributes.get("nickName");
                            return new UserPrincipal(userId, nickName);
                        }
                        // 没有 userId，拒绝连接
                        return null;
                    }
                }
                )
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
