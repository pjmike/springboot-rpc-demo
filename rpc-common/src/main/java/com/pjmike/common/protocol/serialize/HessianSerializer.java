package com.pjmike.common.protocol.serialize;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @description: Hessian 序列化方案经常被RPC框架用来作为默认的序列化方案,比如dubbo，motan
 *
 * @author: pjmike
 * @create: 2019/04/08 19:23
 */
public class HessianSerializer implements Serializer{
    @Override
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArrayOutputStream);
        output.writeObject(object);
        output.flush();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
        return (T) input.readObject(clazz);
    }
}
