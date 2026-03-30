package com.hbpu.aicodebackend.service.impl;

import com.hbpu.aicodebackend.innerservice.InnerUserService;
import com.hbpu.aicodebackend.model.entity.User;
import com.hbpu.aicodebackend.model.vo.UserVO;
import com.hbpu.aicodebackend.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVO(user);
    }

    @Override
    public User getLoginUser(String token) {
        return userService.getLoginUser(token);
    }
}
