package com.pjmike.common.protocol;

import lombok.Data;

/**
 * @description: RPC Response
 * @author: pjmike
 * @create: 2019/03/28 23:01
 */
@Data
public class RpcResponse {
    private String requestId;
    private String error;
    private Object result;
}
