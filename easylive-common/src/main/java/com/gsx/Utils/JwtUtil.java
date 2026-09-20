package com.gsx.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    // @Value 只能注入「实例字段」，static 字段注入不进去
    @Value("${jwt.secret}")
    private String secretKey;


    /**
     * 用配置里的 secret 生成签名密钥。
     * HS256 要求密钥至少 32 字节，所以 yaml 里的 secret 不能太短。
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWS（带 HS256 签名的 token）。payload 里只放不敏感信息。
     */
    public String generateToken(String userId, String nickName,String uuid) {
        return Jwts.builder()
                .id(uuid)
                .subject(userId)
                .claim("nickname", nickName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析并校验签名。签名错误/被篡改/格式不对会抛异常，
     * 过期抛 ExpiredJwtException，由调用方决定怎么处理。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
