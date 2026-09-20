package com.gsx.handler;

import com.gsx.Utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketInterceptor implements HandshakeInterceptor {
    @Autowired
    JwtUtil jwtUtil;
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 1. 从 URL 参数中获取 token
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && !token.isEmpty()) {
                // 兼容两种传法："Bearer xxx" 剥前缀；只传裸 JWT 也直接认（验签本身兜底安全）。
                // 不剥的话 substring(7) 会硬切掉真 token 前 7 个字符，导致解析必失败。
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    Claims claims = jwtUtil.parseToken(token);
                    String userId = claims.getSubject();
                    // 昵称也在 token claim 里，一起带进会话，私聊时给对端显示"谁发的"
                    String nickName = claims.get("nickname", String.class);
                    attributes.put("userId", userId);
                    attributes.put("nickName", nickName);
                    return true;
                } catch (Exception e) {
                    // token 过期或无效，拒绝连接
                    return false;
                }
            }
        }
        return false;
    }
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
