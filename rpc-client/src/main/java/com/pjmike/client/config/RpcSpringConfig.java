package com.pjmike.client.config;

import com.pjmike.client.proxy.ProxyFactory;
import com.pjmike.common.RpcInterface;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/04/07 18:07
 */
//@Configuration
@Slf4j
public class RpcSpringConfig implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;
    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections reflections = new Reflections("com.pjmike");
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        Set<Class<?>> typesAnnotateWith = reflections.getTypesAnnotatedWith(RpcInterface.class);
        for (Class<?> clazz : typesAnnotateWith) {
            beanFactory.registerSingleton(clazz.getSimpleName(), ProxyFactory.create(clazz));
        }
        log.info("afterPropertiesSet is {}",typesAnnotateWith);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
