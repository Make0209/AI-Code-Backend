package com.hbpu.aicodebackend.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用部署请求
 */
@Schema(description = "应用部署请求")
@Data
public class AppDeployRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -1682822898653748928L;

    /**
     * 应用 id
     */
    @Schema(description = "应用 id")
    private Long appId;

}
