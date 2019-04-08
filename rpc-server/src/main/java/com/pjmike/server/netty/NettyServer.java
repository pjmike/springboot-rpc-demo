package com.pjmike.server.netty;


import com.pjmike.common.protocol.RpcDecoder;
import com.pjmike.common.protocol.RpcEncoder;
import com.pjmike.common.protocol.RpcRequest;
import com.pjmike.common.protocol.RpcResponse;
import com.pjmike.common.protocol.serialize.JSONSerializer;
import com.pjmike.common.protocol.serialize.KryoSerializer;
import com.pjmike.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/04/07 17:21
 */
@Component
@Slf4j
public class NettyServer implements InitializingBean {
    private EventLoopGroup boss = null;
    private EventLoopGroup worker = null;

    @Autowired
    private ServerHandler serverHandler;
    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws InterruptedException {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .localAddress(new InetSocketAddress(8887))
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4));
                        pipeline.addLast(new RpcEncoder(RpcResponse.class, new KryoSerializer()));
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new KryoSerializer()));
                        pipeline.addLast(serverHandler);

                    }
                });
        ChannelFuture future = serverBootstrap.bind().sync();
        if (future.isSuccess()) {
            log.info("成功启动 Netty Server");
        }
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        log.info("关闭Netty");
    }
}
