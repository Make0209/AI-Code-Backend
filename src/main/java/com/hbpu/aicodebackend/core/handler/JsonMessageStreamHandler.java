package com.hbpu.aicodebackend.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hbpu.aicodebackend.ai.model.message.AiResponseMessage;
import com.hbpu.aicodebackend.ai.model.message.StreamMessage;
import com.hbpu.aicodebackend.ai.model.message.StreamMessageTypeEnum;
import com.hbpu.aicodebackend.ai.model.message.ThinkingResponseMessage;
import com.hbpu.aicodebackend.ai.model.message.ToolExecutedMessage;
import com.hbpu.aicodebackend.ai.model.message.ToolRequestMessage;
import com.hbpu.aicodebackend.ai.tools.BaseTool;
import com.hbpu.aicodebackend.ai.tools.ToolManager;
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
 * 处理 JSON 格式的流消息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private ToolManager toolManager;

    public Flux<String> handle(Flux<String> originFlux,
            ChatHistoryService chatHistoryService,
            long appId,
            User loginUser) {
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds))
                .filter(StrUtil::isNotEmpty)
                .doOnComplete(() -> {
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(
                            appId,
                            aiResponse,
                            ChatHistoryMessageTypeEnum.AI.getValue(),
                            loginUser.getId()
                    );
                })
                .doOnError(error -> {
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(
                            appId,
                            errorMessage,
                            ChatHistoryMessageTypeEnum.AI.getValue(),
                            loginUser.getId()
                    );
                });
    }

    private String handleJsonMessageChunk(String chunk, StringBuilder historyBuilder, Set<String> seenToolIds) {
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的消息类型");
        }

        return switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                historyBuilder.append(data);
                yield data;
            }
            case THINKING -> {
                ThinkingResponseMessage thinkingMessage = JSONUtil.toBean(chunk, ThinkingResponseMessage.class);
                String thinkingData = thinkingMessage.getData();
                if (StrUtil.isBlank(thinkingData)) {
                    yield "";
                }
                yield "\n\n> 🤔 **AI 正在思考...**\n> " + thinkingData + "\n\n";
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                if (StrUtil.isNotBlank(toolId) && !seenToolIds.add(toolId)) {
                    yield "";
                }
                yield generateCompactToolRequestResponse();
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject arguments = parseArguments(toolExecutedMessage.getArguments());
                BaseTool tool = toolManager.getTool(toolExecutedMessage.getName());

                String renderedResult;
                if (tool != null) {
                    renderedResult = tool.generateToolExecutedResult(arguments, toolExecutedMessage.getResult());
                } else {
                    renderedResult = generateDefaultToolExecutedResult(
                            toolExecutedMessage.getName(),
                            toolExecutedMessage.getResult()
                    );
                }

                String output = "\n\n" + renderedResult + "\n\n";
                historyBuilder.append(output);
                yield output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                yield "";
            }
        };
    }

    private JSONObject parseArguments(String arguments) {
        if (StrUtil.isBlank(arguments)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(arguments);
        } catch (Exception e) {
            log.warn("解析工具参数失败, arguments={}", arguments, e);
            return new JSONObject();
        }
    }

    private String generateCompactToolRequestResponse() {
        return "\n\n<div class=\"tool-status-loading\">\n" +
                "  <span class=\"spin-icon\">⚙️</span>\n" +
                "  <span class=\"status-text\">工具调用中...</span>\n" +
                "</div>\n\n";
    }

    private String generateDefaultToolExecutedResult(String toolName, String result) {
        return String.format(
                "<details class=\"tool-call-block\">\n" +
                        "<summary>\n" +
                        "  <span class=\"chevron-icon\">▼</span>\n" +
                        "  <span class=\"tool-icon\">🧰</span>\n" +
                        "  <span class=\"tool-title\">工具执行：<code>%s</code></span>\n" +
                        "</summary>\n\n" +
                        "> %s\n\n" +
                        "</details>\n",
                StrUtil.blankToDefault(toolName, "unknown_tool"),
                StrUtil.blankToDefault(result, "无返回信息")
        );
    }
}
