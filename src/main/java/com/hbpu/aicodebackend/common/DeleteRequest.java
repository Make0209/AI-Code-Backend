package com.hbpu.aicodebackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除请求包装类
 */
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6587895068354931320L;

    /**
     * id
     */
    private Long id;


}