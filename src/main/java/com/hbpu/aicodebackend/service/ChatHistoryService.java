package com.hbpu.aicodebackend.service;

import com.hbpu.aicodebackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.hbpu.aicodebackend.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.hbpu.aicodebackend.model.entity.ChatHistory;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息。
     *
     * @param appId       应用id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户id
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用id删除对话消息。
     *
     * @param appId 应用id
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询条件。
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据应用id分页获取对话消息。
     *
     * @param appId          应用id
     * @param pageSize       页大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser      登录用户
     * @return 对话消息
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 将对话消息加载到内存中。
     *
     * @param appId       应用id
     * @param chatMemory  对话内存
     * @param maxCount    最大数量
     * @return 加载数量
     */
    int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount);

}
