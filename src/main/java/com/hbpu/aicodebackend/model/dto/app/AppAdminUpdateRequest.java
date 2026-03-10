package com.hbpu.aicodebackend.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用管理更新请求
 *
 * @author Kefan
 */
@Schema(description = "应用管理更新请求")
@Data
public class AppAdminUpdateRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 8063974489692594056L;

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
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;


}
