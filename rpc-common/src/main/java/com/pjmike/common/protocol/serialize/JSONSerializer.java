package com.pjmike.common.protocol.serialize;

import com.alibaba.fastjson.JSON;

/**
 * @description: 使用fastJson作为序列化框架
 * @author: pjmike
 * @create: 2019/03/29 09:41
 */
public class JSONSerializer implements Serializer{

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
