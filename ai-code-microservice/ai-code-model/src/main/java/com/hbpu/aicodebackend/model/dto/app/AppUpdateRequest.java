package com.hbpu.aicodebackend.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用更新请求
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
@Schema(description = "应用更新请求")
@Data
public class AppUpdateRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -2535705105895997990L;

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


}
