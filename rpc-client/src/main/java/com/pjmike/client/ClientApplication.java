package com.pjmike.client;

import com.pjmike.client.proxy.ProxyFactory;
import com.pjmike.common.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
//        ConfigurableApplicationContext context = SpringApplication.run(ClientApplication.class, args);
//        HelloService helloService = context.getBean(HelloService.class);
        HelloService helloService = ProxyFactory.create(HelloService.class);
        log.info("响应结果“: {}",helloService.hello("pjmike"));
    }
}
