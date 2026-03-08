package com.hbpu.aicodebackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成器服务工厂，注册生成一个自定义接口类型的ai服务对象
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    // 聊天模型
    @Resource
    private ChatModel chatModel;

    // 流式聊天模型
    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建AI代码生成器服务, 同时创建一个支持普通输出和流式输出
     * @return  AI代码生成器服务对象
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                         .chatModel(chatModel)
                         .streamingChatModel(streamingChatModel)
                         .build();
    }
}


