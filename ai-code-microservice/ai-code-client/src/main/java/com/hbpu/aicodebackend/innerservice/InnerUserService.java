package com.hbpu.aicodebackend.innerservice;

import com.hbpu.aicodebackend.auth.CurrentUser;
import com.hbpu.aicodebackend.auth.UserContextHolder;
import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import com.hbpu.aicodebackend.model.entity.User;
import com.hbpu.aicodebackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface InnerUserService {

    List<User> listByIds(Collection<? extends Serializable> ids);

    User getById(Serializable id);

    UserVO getUserVO(User user);

    static User getLoginUser(HttpServletRequest request) {
        return getLoginUser();
    }

    static User getLoginUser() {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return User.builder()
                .id(currentUser.getUserId())
                .userAccount(currentUser.getUserAccount())
                .userRole(currentUser.getUserRole())
                .build();
    }
}
