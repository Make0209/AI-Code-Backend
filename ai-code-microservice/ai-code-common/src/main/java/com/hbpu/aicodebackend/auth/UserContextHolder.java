package com.hbpu.aicodebackend.auth;

import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;

public final class UserContextHolder {

    private static final ThreadLocal<CurrentUser> CURRENT_USER_HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void setCurrentUser(CurrentUser currentUser) {
        CURRENT_USER_HOLDER.set(currentUser);
    }

    public static CurrentUser getCurrentUser() {
        return CURRENT_USER_HOLDER.get();
    }

    public static CurrentUser getRequiredUser() {
        CurrentUser currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录或登录信息已失效");
        }
        return currentUser;
    }

    public static void clear() {
        CURRENT_USER_HOLDER.remove();
    }
}
