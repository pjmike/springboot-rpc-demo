package com.pjmike.common.protocol;

import lombok.Data;

/**
 * @description: RPC Response
 * @author: pjmike
 * @create: 2019/03/28 23:01
 */
@Data
public class RpcResponse {
    /**
     * 响应ID
     */
    private String requestId;
    /**
     * 错误信息
     */
    private String error;
    /**
     * 返回的结果
     */
    private Object result;
}
