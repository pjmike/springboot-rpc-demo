package com.pjmike.client.netty;

import com.pjmike.client.future.DefaultFuture;
import com.pjmike.common.protocol.RpcRequest;
import com.pjmike.common.protocol.RpcResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/03/29 11:06
 */
public class ClientHandler extends ChannelDuplexHandler {
    private final Map<String, DefaultFuture> futureMap = new ConcurrentHashMap<>();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcResponse) {
            RpcResponse response = (RpcResponse) msg;
            DefaultFuture defaultFuture = futureMap.get(response.getRequestId());
            defaultFuture.setResponse(response);
        }
        super.channelRead(ctx,msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RpcRequest) {
            RpcRequest request = (RpcRequest) msg;
            futureMap.putIfAbsent(request.getRequestId(), new DefaultFuture());
        }
        super.write(ctx, msg, promise);
    }

    public RpcResponse getRpcResponse(String requsetId) {
        try {
            DefaultFuture future = futureMap.get(requsetId);
            return future.getRpcResponse(5000);
        } finally {
            futureMap.remove(requsetId);
        }
    }
}
