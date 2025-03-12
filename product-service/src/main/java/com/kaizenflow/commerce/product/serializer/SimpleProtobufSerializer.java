package com.kaizenflow.commerce.product.serializer;

import com.google.protobuf.GeneratedMessage;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class SimpleProtobufSerializer<T extends GeneratedMessage> implements Serializer<T> {

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try {
            return data.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Error serializing Protobuf message", e);
        }
    }

}
