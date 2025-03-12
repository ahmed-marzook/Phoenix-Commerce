package com.kaizenflow.commerce.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.product-events}")
    private String topicName;

    @Bean
    public NewTopic productEventsTopic() {
        return TopicBuilder.name(topicName).build();
    }
}
