package com.hbpu.aicodebackend.core;

import cn.hutool.json.JSONUtil;
import com.hbpu.aicodebackend.ai.AiCodeGeneratorService;
import com.hbpu.aicodebackend.ai.AiCodeGeneratorServiceFactory;
import com.hbpu.aicodebackend.ai.model.HtmlCodeResult;
import com.hbpu.aicodebackend.ai.model.MultiFileCodeResult;
import com.hbpu.aicodebackend.ai.model.message.AiResponseMessage;
import com.hbpu.aicodebackend.ai.model.message.ThinkingResponseMessage;
import com.hbpu.aicodebackend.ai.model.message.ToolExecutedMessage;
import com.hbpu.aicodebackend.ai.model.message.ToolRequestMessage;
import com.hbpu.aicodebackend.core.parser.CodeParserExecutor;
import com.hbpu.aicodebackend.core.saver.CodeFileSaverExecutor;
import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import com.hbpu.aicodebackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeFactory;

    /**
     * 统一入口：根据类型生成并保存代码（使用 appId）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据 appId 和生成类型获取对应的 AI 代码生成服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式，使用 appId）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用 ID
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据 appId 和生成类型获取对应的 AI 代码生成服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(codeStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用流式代码处理方法（使用 appId）
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，路径为：{}", savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> tokenStream
                // 1. 处理正常的 AI 文本回复
                .onPartialResponse(partialResponse -> {
                    AiResponseMessage msg = new AiResponseMessage(partialResponse);
                    sink.next(JSONUtil.toJsonStr(msg));
                })
                // 2. 处理 AI 的思考过程（如果你不打算建 ThinkingResponseMessage，可以注释掉这段）
                .onPartialThinking(partialThinking -> {
                    ThinkingResponseMessage msg = new ThinkingResponseMessage(partialThinking.text());
                    sink.next(JSONUtil.toJsonStr(msg));
                })
                // 3. 工具即将执行（此时 AI 已经完整生成了工具调用参数）
                // 完美适配你的 ToolRequestMessage 构造函数
                .beforeToolExecution(beforeExecution -> {
                    // beforeExecution.request() 返回完整的 ToolExecutionRequest
                    ToolRequestMessage msg = new ToolRequestMessage(beforeExecution.request());
                    sink.next(JSONUtil.toJsonStr(msg));
                })
                // 4. 工具执行完毕（此时本地方法已运行完毕并拿到 result）
                // 完美适配你的 ToolExecutedMessage 构造函数
                .onToolExecuted(toolExecution -> {
                    ToolExecutedMessage msg = new ToolExecutedMessage(toolExecution);
                    sink.next(JSONUtil.toJsonStr(msg));
                })
                // 5. 整个流式对话结束
                .onCompleteResponse(chatResponse -> sink.complete())
                // 6. 异常处理
                .onError(error -> {
                    log.error("流式处理出错: {}", error.getMessage());
                    sink.error(error);
                })
                // 必须调用 start 启动流
                .start());
    }
}
