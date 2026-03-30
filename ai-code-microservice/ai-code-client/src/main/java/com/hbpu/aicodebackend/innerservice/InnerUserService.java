package com.hbpu.aicodebackend.innerservice;

import com.hbpu.aicodebackend.model.entity.User;
import com.hbpu.aicodebackend.model.vo.UserVO;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface InnerUserService {

    List<User> listByIds(Collection<? extends Serializable> ids);

    User getById(Serializable id);

    UserVO getUserVO(User user);

    User getLoginUser(String token);
}
