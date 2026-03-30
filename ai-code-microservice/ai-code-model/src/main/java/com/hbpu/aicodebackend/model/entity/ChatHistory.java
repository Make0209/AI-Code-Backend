package com.hbpu.aicodebackend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话历史 实体类。
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
@Schema(description = "对话历史 实体类。")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_history")
public class ChatHistory implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 消息
     */
    @Schema(description = "消息")
    private String message;

    /**
     * user/ai
     */
    @Schema(description = "user/ai")
    @Column("messageType")
    private String messageType;

    /**
     * 应用id
     */
    @Schema(description = "应用id")
    @Column("appId")
    private Long appId;

    /**
     * 创建用户id
     */
    @Schema(description = "创建用户id")
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
