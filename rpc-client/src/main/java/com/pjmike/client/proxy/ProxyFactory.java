package com.pjmike.client.proxy;

import java.lang.reflect.Proxy;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/04/07 17:01
 */
public class ProxyFactory {
    public static <T> T create(Class<T> interfaceClass) throws Exception {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class<?>[] {interfaceClass}, new RpcClientDynamicProxy<T>(interfaceClass));
    }
}
