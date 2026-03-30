package com.hbpu.aicodebackend.auth;

import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public final class JwtUtil {

    private static final String DEFAULT_SECRET_KEY =
            "HBPU_AI_CODE_PROJECT_SECRET_KEY_FOR_JWT_2026";

    private JwtUtil() {
    }

    public static String generateJwt(String userAccount) {
        CurrentUser currentUser = CurrentUser.builder()
                .userAccount(userAccount)
                .build();
        return Jwts.builder()
                .subject(userAccount)
                .claim(JwtClaimsConstant.USER_ACCOUNT, userAccount)
                .issuedAt(new Date())
                .expiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusDays(7).toInstant()))
                .signWith(getSecretKey(DEFAULT_SECRET_KEY))
                .compact();
    }

    public static String generateJwt(String userAccount, Long userId, LocalDateTime expireTime) {
        ZonedDateTime expirationTime = expireTime == null
                ? ZonedDateTime.now(ZoneId.systemDefault()).plusDays(7)
                : expireTime.atZone(ZoneId.systemDefault());
        return Jwts.builder()
                .subject(userAccount)
                .claim(JwtClaimsConstant.USER_ID, userId)
                .claim(JwtClaimsConstant.USER_ACCOUNT, userAccount)
                .issuedAt(new Date())
                .expiration(Date.from(expirationTime.toInstant()))
                .signWith(getSecretKey(DEFAULT_SECRET_KEY))
                .compact();
    }

    public static String generateJwt(CurrentUser currentUser, String secretKey, long expireDays) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成 token 时用户信息不能为空");
        }
        return Jwts.builder()
                .subject(String.valueOf(currentUser.getUserId()))
                .claim(JwtClaimsConstant.USER_ID, currentUser.getUserId())
                .claim(JwtClaimsConstant.USER_ACCOUNT, currentUser.getUserAccount())
                .claim(JwtClaimsConstant.USER_ROLE, currentUser.getUserRole())
                .claim(JwtClaimsConstant.TOKEN_VERSION, currentUser.getTokenVersion())
                .issuedAt(new Date())
                .expiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusDays(expireDays).toInstant()))
                .signWith(getSecretKey(secretKey))
                .compact();
    }

    public static Claims parseToken(String token, String secretKey) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey(secretKey))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 无效或已过期");
        }
    }

    public static Claims parseToken(String token) {
        return parseToken(token, DEFAULT_SECRET_KEY);
    }

    public static boolean isTokenExpired(String token, String secretKey) {
        Date expiration = parseToken(token, secretKey).getExpiration();
        return expiration == null || expiration.before(new Date());
    }

    public static Boolean isTokenExpired(String token) {
        return isTokenExpired(token, DEFAULT_SECRET_KEY);
    }

    public static CurrentUser getCurrentUser(String token, String secretKey) {
        Claims claims = parseToken(token, secretKey);
        return CurrentUser.builder()
                .userId(getLongClaim(claims, JwtClaimsConstant.USER_ID))
                .userAccount(claims.get(JwtClaimsConstant.USER_ACCOUNT, String.class))
                .userRole(claims.get(JwtClaimsConstant.USER_ROLE, String.class))
                .tokenVersion(getIntegerClaim(claims, JwtClaimsConstant.TOKEN_VERSION))
                .build();
    }

    public static String getUserAccount(String token) {
        Claims claims = parseToken(token, DEFAULT_SECRET_KEY);
        String userAccount = claims.get(JwtClaimsConstant.USER_ACCOUNT, String.class);
        return userAccount != null ? userAccount : claims.getSubject();
    }

    private static SecretKey getSecretKey(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "JWT 密钥未配置");
        }
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private static Long getLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static Integer getIntegerClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
