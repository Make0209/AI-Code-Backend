package com.hbpu.aicodebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import com.hbpu.aicodebackend.exception.ThrowUtils;
import com.hbpu.aicodebackend.mapper.UserMapper;
import com.hbpu.aicodebackend.model.dto.UserQueryRequest;
import com.hbpu.aicodebackend.model.entity.User;
import com.hbpu.aicodebackend.model.enums.UserRoleEnum;
import com.hbpu.aicodebackend.model.vo.LoginUserVO;
import com.hbpu.aicodebackend.model.vo.UserVO;
import com.hbpu.aicodebackend.security.JwtUtil;
import com.hbpu.aicodebackend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    public static final String REDIS_KEY = "ai-code:user:login:token:%s";
    private final RedissonClient redisson;

    public UserServiceImpl(RedissonClient redisson) {
        this.redisson = redisson;
    }


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户登录方法实现类
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 用户登录请求响应封装类
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        //验证参数是否为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), new BusinessException(ErrorCode.PARAMS_ERROR));
        //判断用户账号是否过短
        ThrowUtils.throwIf(userAccount.length() < 4, new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短"));
        //判断用户的密码格式是否正确
        ThrowUtils.throwIf(userPassword.length() < 8, new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短！"));
        //获取加密后的密码
        String encryptedPassword = getEncryptPassword(userPassword);
        //从数据库中查询用户
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptedPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            log.error("User not found !");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号或密码错误！");
        }
        //将用户数据映射为请求包装类
        LoginUserVO userLoginVO = new LoginUserVO();
        BeanUtils.copyProperties(user, userLoginVO);
        //为当前用户生成唯一token
        String token = JwtUtil.generateJwt(userAccount);
        //将token放入响应体中
        userLoginVO.setToken(token);
        // 使用用户id拼接来组成独立的每个key
        String redisKey = String.format(REDIS_KEY, userLoginVO.getUserAccount());
        // 创建一个Map
        RMapCache<Object, Object> mapCache = redisson.getMapCache(redisKey);
        // 设置其值
//        mapCache.put("token", token, 30, TimeUnit.MINUTES);
//        mapCache.put("object", user, 30, TimeUnit.MINUTES);
        // 修改后（7天）：
        mapCache.put("token", token, 7, TimeUnit.DAYS);
        mapCache.put("object", user, 7, TimeUnit.DAYS);

        return userLoginVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 获取请求头中的Authorization，因为它一般存储着token
        String authHeader = request.getHeader("Authorization");
        // 判断是否为空，且其中内容是否正确
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 去掉 "Bearer " 前缀
            String token = authHeader.substring(7);
            // 解析token
            Claims claims = JwtUtil.parseToken(token);
            // 使用token中负载的主题来拼接redis的key
            String redisKey = String.format(REDIS_KEY, claims.getSubject());
            // 通过key来获取相应的map和其中存储的对象
            User user = (User) redisson.getMapCache(redisKey).get("object");
            // 判断所获取的对象是否存在
            ThrowUtils.throwIf(user == null, new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
            // 存在则返回处理后的对象
            return user;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取token时出现异常");
        }
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否成功
     */
    @Override
    public Boolean userLogout(HttpServletRequest request) {
        // 获取请求头中的Authorization，因为它一般存储着token
        String authHeader = request.getHeader("Authorization");
        // 判断是否为空，且其中内容是否正确
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 去掉 "Bearer " 前缀
            String token = authHeader.substring(7);
            // 解析token
            Claims claims = JwtUtil.parseToken(token);
            // 使用token中负载的主题来拼接redis的key
            String redisKey = String.format(REDIS_KEY, claims.getSubject());
            // 通过key来获取相应的map并进行删除
            boolean delete = redisson.getMapCache(redisKey).delete();
            if (!delete) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注销失败！");
            }
            return true;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取token时出现错误！");
        }
    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList 用户信息列表
     * @return 脱敏后的用户信息列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                           .eq("id", id)
                           .eq("userRole", userRole)
                           .like("userAccount", userAccount)
                           .like("userName", userName)
                           .like("userProfile", userProfile)
                           .orderBy(sortField, "ascend".equals(sortOrder));
    }




    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "Kefan";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


}
