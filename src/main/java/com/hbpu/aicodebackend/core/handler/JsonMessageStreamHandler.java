package com.hbpu.aicodebackend.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hbpu.aicodebackend.ai.model.message.*;
import com.hbpu.aicodebackend.constant.AppConstant;
import com.hbpu.aicodebackend.core.builder.VueProjectBuilder;
import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import com.hbpu.aicodebackend.model.entity.User;
import com.hbpu.aicodebackend.model.enums.ChatHistoryMessageTypeEnum;
import com.hbpu.aicodebackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    public Flux<String> handle(Flux<String> originFlux,
            ChatHistoryService chatHistoryService,
            long appId, User loginUser) {
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty)
                .doOnComplete(() -> {
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(
                            appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(
                            appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        if (typeEnum != null) {
            switch (typeEnum) {
                case AI_RESPONSE -> {
                    AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                    String data = aiMessage.getData();
                    chatHistoryStringBuilder.append(data);
                    return data;
                }
                case THINKING -> {
                    ThinkingResponseMessage thinkingMessage = JSONUtil.toBean(chunk, ThinkingResponseMessage.class);
                    String thinkingData = thinkingMessage.getData();
                    if (StrUtil.isBlank(thinkingData)) {
                        return "";
                    }
                    // 模拟大模型思考过程的视觉效果
                    return "\n\n> 💭 **AI 正在思考...**\n> " + thinkingData + "\n\n";
                }
                case TOOL_REQUEST -> {
                    ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                    String toolId = toolRequestMessage.getId();
                    if (toolId != null && !seenToolIds.contains(toolId)) {
                        seenToolIds.add(toolId);
                        // 统一使用 class 标识，去除了文字颜色，交给前端处理默认颜色
                        return "\n\n<div class=\"tool-status-loading\">\n" +
                                "  <span class=\"spin-icon\">⚙️</span>\n" +
                                "  <span class=\"status-text\">AI 正在调用工具：准备写入文件...</span>\n" +
                                "</div>\n\n";
                    } else {
                        return "";
                    }
                }
                case TOOL_EXECUTED -> {
                    ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                    JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                    String relativeFilePath = jsonObject.getStr("relativeFilePath");
                    String suffix = FileUtil.getSuffix(relativeFilePath);
                    String content = jsonObject.getStr("content");

                    // 结构化 HTML 用于前端展开收起
                    String result = String.format(
                            "<details class=\"tool-call-block\">\n" +
                                    "<summary>\n" +
                                    "  <span class=\"chevron-icon\">▶</span>\n" +
                                    "  <span class=\"tool-icon\">🛠️</span>\n" +
                                    "  <span class=\"tool-title\">成功写入文件: <code>%s</code></span>\n" +
                                    "</summary>\n\n" +
                                    "```%s\n%s\n```\n\n" +
                                    "</details>\n",
                            relativeFilePath, suffix, content
                    );
                    String output = String.format("\n\n%s\n\n", result);
                    chatHistoryStringBuilder.append(output);
                    return output;
                }
                default -> {
                    log.error("不支持的消息类型: {}", typeEnum);
                    return "";
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的消息类型");
    }
}