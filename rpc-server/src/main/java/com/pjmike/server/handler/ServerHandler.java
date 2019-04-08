package com.pjmike.server.handler;

import com.pjmike.common.protocol.RpcRequest;
import com.pjmike.common.protocol.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/04/07 17:41
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(msg.getRequestId());
        try {
            Object handler = handler(msg);
            log.info("获取返回结果: {} ", handler);
            rpcResponse.setResult(handler);
        } catch (Throwable throwable) {
            rpcResponse.setError(throwable.toString());
            throwable.printStackTrace();
        }
        ctx.writeAndFlush(rpcResponse);
    }

    /**
     * 服务端使用代理处理请求
     *
     * @param request
     * @return
     */
    private Object handler(RpcRequest request) throws ClassNotFoundException, InvocationTargetException {
        //使用Class.forName进行加载Class文件
        Class<?> clazz = Class.forName(request.getClassName());
        Object serviceBean = applicationContext.getBean(clazz);
        log.info("serviceBean: {}",serviceBean);
        Class<?> serviceClass = serviceBean.getClass();
        log.info("serverClass:{}",serviceClass);
        String methodName = request.getMethodName();

        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //使用CGLIB Reflect
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
        log.info("开始调用CGLIB动态代理执行服务端方法...");
        return fastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
