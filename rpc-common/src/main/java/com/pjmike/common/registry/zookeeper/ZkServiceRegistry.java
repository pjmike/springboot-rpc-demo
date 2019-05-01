package com.pjmike.common.registry.zookeeper;

import com.pjmike.common.bean.Constant;
import com.pjmike.common.registry.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;

/**
 * @description:
 * @author: 13572
 * @create: 2019/04/29 22:03
 */
public class ZkServiceRegistry implements ServiceRegistry {

    private final CuratorFramework curatorFramework;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    public ZkServiceRegistry(String address) {
        this.curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(address)
                .sessionTimeoutMs(Constant.ZK_SESSION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
    }

    @Override
    public void registry(String data) throws Exception {
        curatorFramework.start();

        String path = Constant.ZK_CHILDREN_PATH;
        curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data.getBytes());

    }
}
