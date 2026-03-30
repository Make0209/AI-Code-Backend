package com.hbpu.aicodebackend.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 思考过程消息 (适配 DeepSeek R1 等推理模型)
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ThinkingResponseMessage extends StreamMessage {

    private String data;

    public ThinkingResponseMessage(String data) {
        super(StreamMessageTypeEnum.THINKING.getValue());
        this.data = data;
    }
}