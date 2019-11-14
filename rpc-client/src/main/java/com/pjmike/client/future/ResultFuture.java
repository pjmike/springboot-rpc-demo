package com.pjmike.client.future;

import com.pjmike.common.protocol.RpcResponse;
import io.netty.util.concurrent.DefaultPromise;

/**
 * Future有几种模式：
 * 1. JDK自带的Future: {@link java.util.concurrent.Future}
 * 2. 自定义Future: 比如{@link DefaultFuture}
 * 3. 利用Netty的DefaultPromise : {@link DefaultPromise}
 * 4. Java 8 的 {@link java.util.concurrent.CompletableFuture}
 *
 * @author: pjmike
 * @create: 2019/11/13
 */
public class ResultFuture extends DefaultPromise<RpcResponse> {
}
