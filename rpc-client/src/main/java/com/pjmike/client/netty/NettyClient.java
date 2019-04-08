package com.pjmike.client.netty;

import com.pjmike.common.protocol.RpcDecoder;
import com.pjmike.common.protocol.RpcEncoder;
import com.pjmike.common.protocol.RpcRequest;
import com.pjmike.common.protocol.RpcResponse;
import com.pjmike.common.protocol.serialize.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/03/29 10:58
 */
@Slf4j
public class NettyClient {
    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private ClientHandler clientHandler;
    private String host;
    private Integer port;
    public NettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
    public void connect() {
        clientHandler = new ClientHandler();
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4));
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new KryoSerializer()));
                        pipeline.addLast(new RpcDecoder(RpcResponse.class, new KryoSerializer()));
                        pipeline.addLast(clientHandler);
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
                log.info("连接Netty服务端成功");
            } else {
                log.info("连接失败，进行断线重连");
                future.channel().eventLoop().schedule(() -> connect(), 20, TimeUnit.SECONDS);
            }
            channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public RpcResponse send(final RpcRequest request) {
        try {
            channel.writeAndFlush(request).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientHandler.getRpcResponse(request.getRequestId());
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }
}
