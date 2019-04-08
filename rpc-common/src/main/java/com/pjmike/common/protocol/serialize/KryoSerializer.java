package com.pjmike.common.protocol.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @description:
 * @author: pjmike
 * @create: 2019/04/08 18:57
 */
public class KryoSerializer implements Serializer{
    /**
     * Kryo不是线程安全的，使用ThreadLocal来保证其线程安全
     *
     */
    private static final ThreadLocal<Kryo> kryos = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        //支持循环引用
        kryo.setReferences(true);
        //关闭注册行为
        kryo.setRegistrationRequired(false);
        return kryo;
    });
    @Override
    public byte[] serialize(Object object) {
        Kryo kryo = kryos.get();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeClassAndObject(output, object);
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        Kryo kryo = kryos.get();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);
        input.close();
        return (T) kryo.readClassAndObject(input);
    }
}
