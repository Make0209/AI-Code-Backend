package com.hbpu.aicodebackend.auth;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static String extractToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String authHeader = request.getHeader(AuthConstant.AUTHORIZATION_HEADER);
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(AuthConstant.BEARER_PREFIX)) {
            return authHeader.substring(AuthConstant.BEARER_PREFIX.length());
        }
        String token = request.getParameter("token");
        return StrUtil.isNotBlank(token) ? token : null;
    }
}
