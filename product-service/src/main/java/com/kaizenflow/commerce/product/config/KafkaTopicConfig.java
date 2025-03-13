package com.kaizenflow.commerce.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.product-created-events}")
    private String productCreatedEventsTopic;

    @Value("${kafka.topic.product-updated-events}")
    private String productUpdatedEventsTopic;

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(productCreatedEventsTopic).build();
    }

    @Bean
    public NewTopic productUpdatedEventsTopic() {
        return TopicBuilder.name(productUpdatedEventsTopic).build();
    }
}
