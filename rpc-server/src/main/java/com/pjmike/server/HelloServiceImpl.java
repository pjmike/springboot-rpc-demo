package com.pjmike.server;

import com.pjmike.common.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/04/08 16:44
 */
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello, " + name;
    }
}
