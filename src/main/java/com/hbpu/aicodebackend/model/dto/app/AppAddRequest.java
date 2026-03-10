package com.hbpu.aicodebackend.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 添加应用请求
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
@Schema(description = "添加应用请求")
@Data
public class AppAddRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -726797905250257608L;

    /**
     * 应用初始化的 prompt
     */
    @Schema(description = "应用初始化的 prompt")
    private String initPrompt;

}
