package com.hbpu.aicodebackend.langgraph4j.ai;

import com.hbpu.aicodebackend.langgraph4j.model.ImageCollectionPlan;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ImageCollectionPlanService {

    /**
     * 根据用户提示词分析需要收集的图片类型和参数
     */
    @SystemMessage(fromResource = "prompt/image-collection-plan-system-prompt.md")
    ImageCollectionPlan planImageCollection(@UserMessage String userPrompt);
}
