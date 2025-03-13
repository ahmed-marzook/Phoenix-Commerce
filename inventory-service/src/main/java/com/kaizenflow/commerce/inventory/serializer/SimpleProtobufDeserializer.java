package com.kaizenflow.commerce.inventory.serializer;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Parser;

public class SimpleProtobufDeserializer<T extends GeneratedMessage> implements Deserializer<T> {

    private final Parser<T> parser;

    /**
     * Create a deserializer for a specific Protobuf message type.
     *
     * @param parser The parser for the specific message type, e.g., AddressBook.parser()
     */
    public SimpleProtobufDeserializer(Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return parser.parseFrom(data);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing Protobuf message", e);
        }
    }
}
