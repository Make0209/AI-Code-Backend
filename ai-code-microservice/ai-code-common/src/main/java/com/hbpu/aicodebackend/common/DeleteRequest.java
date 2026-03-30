package com.hbpu.aicodebackend.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除请求包装类
 */
@Schema(description = "删除请求包装类")
@Data
public class DeleteRequest implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 6587895068354931320L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;


}