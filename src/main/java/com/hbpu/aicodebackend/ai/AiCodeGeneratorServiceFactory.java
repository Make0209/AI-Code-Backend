package com.hbpu.aicodebackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hbpu.aicodebackend.ai.tools.FileWriteTool;
import com.hbpu.aicodebackend.config.ReasoningStreamingChatModelConfig;
import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import com.hbpu.aicodebackend.model.enums.CodeGenTypeEnum;
import com.hbpu.aicodebackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 代码生成器服务工厂类
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource(name = "openAiStreamingChatModel")
    private StreamingChatModel openAiStreamingChatModel;

    @Resource(name = "reasoningStreamingChatModel")
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
                                                                               .maximumSize(1000)
                                                                               .expireAfterWrite(Duration.ofMinutes(30))
                                                                               .expireAfterAccess(
                                                                                       Duration.ofMinutes(10))
                                                                               .removalListener((key, value, cause) -> {
                                                                                   log.debug(
                                                                                           "AI 服务实例被移除，缓存键: {}, 原因: {}",
                                                                                           key, cause
                                                                                   );
                                                                               })
                                                                               .build();

    /**
     * 根据 appId 获取服务（带缓存）这个方法是为了兼容历史逻辑
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 和代码生成类型获取服务（带缓存）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }


    /**
     * 根据应用程序的 ID 和代码生成类型创建 AI 代码生成器服务
     *
     * @param appId       应用程序的 ID
     * @param codeGenType 代码生成类型
     * @return AI 代码生成器服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        // 根据代码生成类型选择不同的模型配置
        return switch (codeGenType) {
            // Vue 项目生成使用推理模型
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                                          // 使用推理流式模型
                                          .streamingChatModel(reasoningStreamingChatModel)
                                          // 使用 chatMemoryProvider 而不是 chatMemory，因为需要根据 appId 获取不同的 chatMemory
                                          .chatMemoryProvider(memoryId -> chatMemory)
                                          // 添加文件写入工具，用于将生成的代码保存到文件中
                                          .tools(new FileWriteTool())
                                          // 处理工具名称不存在的情况，返回错误信息给AI
                                          .hallucinatedToolNameStrategy(
                                                  toolExecutionRequest -> ToolExecutionResultMessage.from(
                                                          toolExecutionRequest,
                                                          "Error: there is no tool called " + toolExecutionRequest.name()
                                                  ))
                                          .build();
            // HTML 和多文件生成使用默认模型
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                                               .chatModel(chatModel)
                                               .streamingChatModel(openAiStreamingChatModel)
                                               .chatMemory(chatMemory)
                                               .build();
            default -> throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "不支持的代码生成类型: " + codeGenType.getValue()
            );
        };
    }


}
