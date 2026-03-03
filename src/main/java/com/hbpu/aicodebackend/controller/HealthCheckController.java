package com.hbpu.aicodebackend.controller;

import com.hbpu.aicodebackend.common.BaseResponse;
import com.hbpu.aicodebackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 */
@Tag(name = "HealthCheckController", description = "健康检查")
@RestController
@RequestMapping("/health")
public class HealthCheckController {
    /**
     * 健康检查
     *
     * @return "OK"
     */
    @Operation(summary = "健康检查", description = "健康检查")
    @GetMapping("/check")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("OK");
    }
}
