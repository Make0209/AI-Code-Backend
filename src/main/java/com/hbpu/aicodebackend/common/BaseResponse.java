package com.hbpu.aicodebackend.common;

import com.hbpu.aicodebackend.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;import java.io.Serializable;

/**
 * 通过响应类
 *
 * @param <T>
 */
@Schema(description = "通过响应类")
@Data
public class BaseResponse<T> implements Serializable {

    @Schema(hidden = true)
    @Serial
    private static final long serialVersionUID = -2362061265069665413L;

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private int code;

    /**
     * 数据
     */
    @Schema(description = "数据")
    private T data;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息")
    private String message;

    /**
     * 自定义返回信息构造器
     * @param code 状态码
     * @param data 数据
     * @param message 提示信息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 自定义返回信息构造器
     * @param code 状态码
     * @param data 数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 自定义返回信息构造器
     * @param errorCode 错误码
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
