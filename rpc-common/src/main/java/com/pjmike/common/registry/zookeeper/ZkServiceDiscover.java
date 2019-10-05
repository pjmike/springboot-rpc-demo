package com.pjmike.common.registry.zookeeper;

import com.pjmike.common.bean.Constant;
import com.pjmike.common.registry.ServiceDiscover;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description: 服务发现类
 * @author: 13572
 * @create: 2019/04/29 22:40
 */
public class ZkServiceDiscover implements ServiceDiscover {
    private final CuratorFramework curatorFramework;
    private volatile List<String> dataList = new ArrayList<>();
    public ZkServiceDiscover(String zkAddress) throws Exception {
        this.curatorFramework = CuratorFrameworkFactory
                .builder()
                .connectString(zkAddress)
                .connectionTimeoutMs(Constant.ZK_CONNECTION_TIMEOUT)
                .sessionTimeoutMs(Constant.ZK_SESSION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        watchChildNode(curatorFramework);
    }

    private void watchChildNode(final CuratorFramework client) throws Exception {
        client.start();
        List<String> nodeList = client.getChildren().usingWatcher((Watcher) watchedEvent -> {
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                try {
                    watchChildNode(client);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).forPath(Constant.ZK_REGISTRY_PATH);

        List<String> dataList = new ArrayList<>();
        for (String node : nodeList) {
            byte[] data = client.getData().forPath(Constant.ZK_REGISTRY_PATH + "/" + node);
            dataList.add(new String(data));
        }
        this.dataList = dataList;
    }

    @Override
    public String discover(){
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
            }
        }
        return data;
    }
}
