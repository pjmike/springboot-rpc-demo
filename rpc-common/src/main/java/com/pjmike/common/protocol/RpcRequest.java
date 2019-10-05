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
    /**
     * 请求对象的ID
     */
    private String requestId;
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 入参
     */
    private Object[] parameters;
}
