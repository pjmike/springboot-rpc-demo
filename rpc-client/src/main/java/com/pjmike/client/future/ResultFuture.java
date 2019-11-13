package com.pjmike.client.future;

import com.pjmike.common.protocol.RpcResponse;
import io.netty.util.concurrent.DefaultPromise;

/**
 * @description: Future模式另一种自定义实现，利用Netty的DefaultPromise
 *
 * @author: pjmike
 * @create: 2019/11/13
 */
public class ResultFuture extends DefaultPromise<RpcResponse> {
}
