package com.hbpu.aicodebackend.controller;

import com.hbpu.aicodebackend.annotation.AuthCheck;
import com.hbpu.aicodebackend.common.BaseResponse;
import com.hbpu.aicodebackend.common.ResultUtils;
import com.hbpu.aicodebackend.constant.UserConstant;
import com.hbpu.aicodebackend.exception.ErrorCode;
import com.hbpu.aicodebackend.exception.ThrowUtils;
import com.hbpu.aicodebackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.hbpu.aicodebackend.model.entity.User;
import com.hbpu.aicodebackend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.hbpu.aicodebackend.model.entity.ChatHistory;
import com.hbpu.aicodebackend.service.ChatHistoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/Make0209">Kefan</a>
 */
@Tag(name = "ChatHistoryController", description = "对话历史 控制层。")
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @Parameters({
            @Parameter(name = "appId", description = "应用ID", in = ParameterIn.PATH, required = true),
            @Parameter(name = "pageSize", description = "页面大小", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "lastCreateTime", description = "最后一条记录的创建时间", in = ParameterIn.QUERY)
    })
    @Operation(summary = "分页查询某个应用的对话历史（游标查询）", description = "分页查询某个应用的对话历史（游标查询）")
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) LocalDateTime lastCreateTime,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(
                appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @Operation(summary = "管理员分页查询所有对话历史", description = "管理员分页查询所有对话历史")
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

}
