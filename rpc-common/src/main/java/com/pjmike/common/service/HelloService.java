package com.pjmike.common.service;

import com.pjmike.common.RpcInterface;

@RpcInterface
public interface HelloService {
    String hello(String name);
}
