package com.hbpu.aicodebackend.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ReasoningStreamingChatModelConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    // 供工厂类注入判断
    // 新增：是否是推理模型，yaml 里控制
    @Value("${ai.reasoning.is-reasoning-model:false}")
    private boolean isReasoningModel;

    @Bean("reasoningStreamingChatModel")
    public StreamingChatModel reasoningStreamingChatModel() {
        final String modelName = isReasoningModel ? "deepseek-reasoner" : "deepseek-chat";
        final int maxTokens = isReasoningModel ? 32768 : 8192;
        return OpenAiStreamingChatModel.builder()
                                       .apiKey(apiKey)
                                       .baseUrl(baseUrl)
                                       .modelName(modelName)
                                       .maxTokens(maxTokens)
                                       .timeout(Duration.ofSeconds(600))
                                       .logRequests(true)
                                       .logResponses(true)
                                       .build();
    }

}