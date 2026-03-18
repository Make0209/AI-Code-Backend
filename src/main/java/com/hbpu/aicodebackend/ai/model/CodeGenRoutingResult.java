package com.hbpu.aicodebackend.ai.model;

import com.hbpu.aicodebackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * AI代码生成类型路由结果
 */
@Description("AI代码生成类型路由结果")
public record CodeGenRoutingResult(
    /*
      推荐的代码生成类型
     */
    @Description("推荐的代码生成类型")
    CodeGenTypeEnum codeGenType,

    /*
      根据用户需求总结的项目名称（简短、有意义，如 "TodoList应用"、"企业官网"）
     */
    @Description("根据用户需求总结的项目名称（简短、有意义，如 \"TodoList应用\"、\"企业官网\"）")
    String projectName
) {}