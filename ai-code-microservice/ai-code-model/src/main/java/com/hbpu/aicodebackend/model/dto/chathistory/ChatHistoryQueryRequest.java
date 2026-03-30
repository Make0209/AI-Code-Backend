package com.hbpu.aicodebackend.model.dto.chathistory;

import com.hbpu.aicodebackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询消息记录请求参数
 *
 * @author Kefan
 */
@Schema(description = "查询消息记录请求参数")
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -158517717673092850L;
    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String message;

    /**
     * 消息类型（user/ai）
     */
    @Schema(description = "消息类型（user/ai）")
    private String messageType;

    /**
     * 应用id
     */
    @Schema(description = "应用id")
    private Long appId;

    /**
     * 创建用户id
     */
    @Schema(description = "创建用户id")
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于此时间的记录
     */
    @Schema(description = "游标查询 - 最后一条记录的创建时间 用于分页查询，获取早于此时间的记录")
    private LocalDateTime lastCreateTime;

}
