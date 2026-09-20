

package com.gsx.handler;

import com.gsx.Utils.JwtUtil;
import com.gsx.Utils.RedisUtil;
import com.gsx.Utils.ResultUtil.ResultCode;
import com.gsx.handler.ExceptionHandler.ServiceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MyInterceptor implements HandlerInterceptor {

    RedisUtil redisUtil;
    JwtUtil jwtUtil;
    MyInterceptor(@Autowired JwtUtil jwtUtil , @Autowired RedisUtil redisUtil){
        this.redisUtil = redisUtil;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 视频的 GET 接口（列表 /video/list、详情 /video/1、互动状态 /video/interact/1）
        // 公开，不需要登录也能看。getServletPath() 返回不含 context-path 的路径（如 /video/list）。
        // 注意 /video/publish 是 POST，不满足此条件，仍走下面的登录校验。
        String servletPath = request.getServletPath();
        if ("GET".equalsIgnoreCase(request.getMethod()) && servletPath.startsWith("/video")) {
            // 可选登录：公开接口也试着解析一次 token。
            // 这样登录用户看详情页时，/video/interact/1 能知道"我点过赞没"；
            // 没带 token 或 token 无效就当匿名处理，不阻断公开访问。
            tryOptionalLogin(request);
            return true;
        }

        String token = request.getHeader("token");
        if(token == null||!token.startsWith("Bearer ")){
            throw new ServiceException(ResultCode.UNAUTHORIZED);
        }
        token = token.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            String jti = claims.getId();
            String userId = claims.getSubject();
            String value = (String) redisUtil.get(userId);
            if(value == null||!value.equals(jti)){
                throw new ServiceException(ResultCode.UNAUTHORIZED);
            }
            UserHolder.setUserId(userId);
            return true;
        } catch (ExpiredJwtException e) {
            throw new ServiceException(ResultCode.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 可选登录解析：请求带了有效 token 就把 userId 放进 UserHolder，
     * 没带或解析失败就静默跳过（当作匿名用户），不抛异常。
     */
    private void tryOptionalLogin(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || !token.startsWith("Bearer ")) {
            return;
        }
        try {
            Claims claims = jwtUtil.parseToken(token.substring(7));
            UserHolder.setUserId(claims.getSubject());
        } catch (Exception e) {
            // token 过期/无效就当作未登录，公开接口照样放行
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
