package com.kaizenflow.commerce.product.config;

import java.util.HashMap;
import java.util.Map;

import com.kaizenflow.commerce.proto.inventory.InventoryUpdateEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.kaizenflow.commerce.product.serializer.SimpleProtobufDeserializer;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, InventoryUpdateEvent> inventoryUpdateEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "product-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SimpleProtobufDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new SimpleProtobufDeserializer<>(InventoryUpdateEvent.parser()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryUpdateEvent>
            inventoryUpdateEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InventoryUpdateEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(inventoryUpdateEventConsumerFactory());
        return factory;
    }
}
