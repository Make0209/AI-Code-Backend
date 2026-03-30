package com.hbpu.aicodebackend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        CurrentUser currentUser = resolveFromToken(request);
        if (currentUser != null && currentUser.getUserId() != null) {
            UserContextHolder.setCurrentUser(currentUser);
            return true;
        }

        String userIdHeader = request.getHeader(AuthConstant.USER_ID_HEADER);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            UserContextHolder.clear();
            return true;
        }
        currentUser = CurrentUser.builder()
                .userId(parseLong(userIdHeader))
                .userAccount(request.getHeader(AuthConstant.USER_ACCOUNT_HEADER))
                .userRole(request.getHeader(AuthConstant.USER_ROLE_HEADER))
                .tokenVersion(parseInteger(request.getHeader(AuthConstant.TOKEN_VERSION_HEADER)))
                .build();
        UserContextHolder.setCurrentUser(currentUser);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private CurrentUser resolveFromToken(HttpServletRequest request) {
        String authorization = request.getHeader(AuthConstant.AUTHORIZATION_HEADER);
        String token = null;
        if (StringUtils.hasText(authorization) && authorization.startsWith(AuthConstant.BEARER_PREFIX)) {
            token = authorization.substring(AuthConstant.BEARER_PREFIX.length());
        } else {
            String tokenParam = request.getParameter("token");
            if (StringUtils.hasText(tokenParam)) {
                token = tokenParam;
            }
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return JwtUtil.getCurrentUser(token);
        } catch (Exception e) {
            UserContextHolder.clear();
            return null;
        }
    }
}
