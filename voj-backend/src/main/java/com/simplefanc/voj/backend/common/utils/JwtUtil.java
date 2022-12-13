package com.simplefanc.voj.backend.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j(topic = "voj")
@Data
@Component
@ConfigurationProperties(prefix = "voj.jwt")
public class JwtUtil {

    public final static String TOKEN_KEY = "token-key:";

    public final static String TOKEN_REFRESH = "token-refresh:";

    private long expire;

    private long checkRefreshExpire;

    private String secret;

    private final RedisUtil redisUtil;

    /**
     * 生成jwt token
     */
    public String generateToken(String userId) {
        // 过期时间
        Date expireTime = new Date(System.currentTimeMillis() + expire * 1000);
        String token = JWT.create()
                .withSubject(userId)
                .withExpiresAt(expireTime)
                .sign(Algorithm.HMAC256(secret));
        redisUtil.set(TOKEN_REFRESH + userId, token, checkRefreshExpire);
        redisUtil.set(TOKEN_KEY + userId, token, expire);
        return token;
    }

    public String getClaimByToken(String token) {
        return JWT.decode(token)
                .getSubject();
    }

    /**
     * token是否合法&过期
     * 抛异常
     */
    public boolean verifyToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            return false;
        }
        return true;
    }
}
