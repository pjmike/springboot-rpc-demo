package com.pjmike.common.protocol;

import lombok.Data;
import lombok.ToString;

/**
 * @description: RPC Request
 * @author: pjmike
 * @create: 2019/03/28 22:58
 */
@Data
@ToString
public class RpcRequest {
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
