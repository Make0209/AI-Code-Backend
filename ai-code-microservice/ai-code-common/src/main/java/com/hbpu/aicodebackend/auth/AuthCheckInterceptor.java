package com.hbpu.aicodebackend.auth;

import com.hbpu.aicodebackend.annotation.AuthCheck;
import com.hbpu.aicodebackend.constant.UserConstant;
import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        AuthCheck authCheck = handlerMethod.getMethodAnnotation(AuthCheck.class);
        if (authCheck == null) {
            return true;
        }

        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            return true;
        }
        String mustRole = authCheck.mustRole();
        if (mustRole == null || mustRole.isBlank()) {
            return true;
        }
        if (UserConstant.ADMIN_ROLE.equals(currentUser.getUserRole())) {
            return true;
        }
        if (!mustRole.trim().equals(currentUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该接口");
        }
        return true;
    }
}
