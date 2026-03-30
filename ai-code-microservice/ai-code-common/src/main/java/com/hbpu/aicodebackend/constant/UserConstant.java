package com.hbpu.aicodebackend.constant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户相关常量
 */
@Schema(description = "endregion")
public interface UserConstant {

    // region 权限

    /**
     * 普通用户
     */
    @Schema(description = "普通用户")
    String USER_ROLE = "user";

    /**
     * 管理员
     */
    @Schema(description = "管理员")
    String ADMIN_ROLE = "admin";

    // endregion
}
