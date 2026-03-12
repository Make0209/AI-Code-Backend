package com.hbpu.aicodebackend.service;

import com.hbpu.aicodebackend.model.dto.app.AppQueryRequest;
import com.hbpu.aicodebackend.model.entity.User;
import com.hbpu.aicodebackend.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.hbpu.aicodebackend.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用VO
     *
     * @param app 应用
     * @return 应用VO
     */
    AppVO getAppVO(App app);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 应用查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用VO列表
     *
     * @param appList 应用列表
     * @return 应用VO列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 聊天生成代码 (已支持保存到历史聊天记录)
     *
     * @param appId     应用id
     * @param message   消息
     * @param loginUser 登录用户
     * @return 代码
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 部署应用
     *
     * @param appId     应用id
     * @param loginUser 登录用户
     * @return 部署结果
     */
    String deployApp(Long appId, User loginUser);
}
