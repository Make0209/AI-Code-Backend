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

import javax.crypto.KeyGenerator;

/**
 * 用户 实体类。
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
@Schema(description = "用户 实体类。")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

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
     * 账号
     */
    @Schema(description = "账号")
    @Column("userAccount")
    private String userAccount;

    /**
     * 密码
     */
    @Schema(description = "密码")
    @Column("userPassword")
    private String userPassword;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    @Column("userProfile")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Schema(description = "用户角色：user/admin")
    @Column("userRole")
    private String userRole;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    @Column("editTime")
    private LocalDateTime editTime;

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
