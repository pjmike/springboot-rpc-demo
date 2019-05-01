package com.pjmike.common.bean;

/**
 * @description: 常量
 * @author: 13572
 * @create: 2019/04/29 22:13
 */
public class Constant {
    public static final int ZK_SESSION_TIMEOUT = 5000;
    public static final int ZK_CONNECTION_TIMEOUT = 5000;
    public static final String ZK_REGISTRY_PATH = "/registry";
    public static final String ZK_CHILDREN_PATH = ZK_REGISTRY_PATH + "/data";
}
