package com.pjmike.client.future;

import com.pjmike.common.protocol.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: pjmike
 * @create: 2019/11/13
 */
public class PendingResult {
    private Map<String, ResultFuture> map = new ConcurrentHashMap<>();

    public void add(String id, ResultFuture future) {
        this.map.put(id, future);
    }

    public void set(String id, RpcResponse response) {
        ResultFuture resultFuture = this.map.get(id);
        if (resultFuture != null) {
            resultFuture.setSuccess(response);
            this.map.remove(id);
        }
    }
}
