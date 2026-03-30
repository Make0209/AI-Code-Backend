package com.hbpu.aicodebackend.model.dto.app;

import com.hbpu.aicodebackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用查询请求
 *
 * @author Kefan
 **/
@Schema(description = "应用查询请求")
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 5027877719206143827L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用封面
     */
    @Schema(description = "应用封面")
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    @Schema(description = "应用初始化的 prompt")
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    @Schema(description = "代码生成类型（枚举）")
    private String codeGenType;

    /**
     * 部署标识
     */
    @Schema(description = "部署标识")
    private String deployKey;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 创建用户id
     */
    @Schema(description = "创建用户id")
    private Long userId;


}
