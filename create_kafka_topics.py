#!/usr/bin/env python3
"""
Kafka Topics Creation Script for Phoenix Commerce using Python
-----------------------------------------------------------
This script creates the necessary Kafka topics for the phoenix commerce.
Follows the existing naming convention (e.g., 'product-created' instead of 'product-created-events').
Requirements: kafka-python package (pip install kafka-python)
"""

import os
import time
from kafka.admin import KafkaAdminClient, NewTopic
from kafka.errors import TopicAlreadyExistsError

# Configuration (from environment variables or defaults)
BOOTSTRAP_SERVERS = os.environ.get('BOOTSTRAP_SERVERS', 'localhost:29092')
REPLICATION_FACTOR = int(os.environ.get('REPLICATION_FACTOR', 1))  # Use 3 for production
PARTITIONS = int(os.environ.get('PARTITIONS', 3))
RETENTION_MS = 604800000  # 7 days in milliseconds

def create_topics():
    """Create all required Kafka topics for the phoenix commerce."""
    
    # List of topics with their configurations (following existing naming scheme)
    topics = [
        # Order-related topics
        {"name": "order-created", "config": {"retention.ms": str(RETENTION_MS)}},
        {"name": "order-updated", "config": {"retention.ms": str(RETENTION_MS)}},
        {"name": "order-cancelled", "config": {"retention.ms": str(RETENTION_MS)}},
        
        # Payment-related topics
        {"name": "payment-confirmed", "config": {"retention.ms": str(RETENTION_MS)}},
        {"name": "payment-failed", "config": {"retention.ms": str(RETENTION_MS)}},
        
        # Shipping-related topics
        {"name": "order-shipped", "config": {"retention.ms": str(RETENTION_MS)}},
        {"name": "order-delivered", "config": {"retention.ms": str(RETENTION_MS)}},
        
        # Following your existing naming scheme like product-created
        {"name": "product-created", "config": {"retention.ms": str(RETENTION_MS)}},
        {"name": "product-updated", "config": {"retention.ms": str(RETENTION_MS)}}
    ]
    
    # Create admin client
    admin_client = KafkaAdminClient(
        bootstrap_servers=BOOTSTRAP_SERVERS,
        client_id='phoenix-commerce-admin'
    )
    
    # Convert topic dictionaries to NewTopic objects
    new_topics = [
        NewTopic(
            name=topic["name"],
            num_partitions=PARTITIONS,
            replication_factor=REPLICATION_FACTOR,
            topic_configs=topic["config"]
        ) for topic in topics
    ]
    
    # Create topics
    created_topics = []
    for topic in new_topics:
        try:
            admin_client.create_topics([topic])
            created_topics.append(topic.name)
            print(f"Created topic: {topic.name}")
        except TopicAlreadyExistsError:
            print(f"Topic {topic.name} already exists")
    
    # Close the admin client
    admin_client.close()
    
    return created_topics

def list_topics():
    """List all Kafka topics to verify creation."""
    from kafka import KafkaConsumer
    
    # Create a consumer to list topics
    consumer = KafkaConsumer(bootstrap_servers=BOOTSTRAP_SERVERS)
    topics = consumer.topics()
    consumer.close()
    
    print("\nAvailable Kafka topics:")
    for topic in sorted(topics):
        print(f"- {topic}")
    
    return topics

if __name__ == "__main__":
    print("Creating Kafka topics for Phoenix Commerce...")
    
    try:
        created_topics = create_topics()
        print(f"\nSuccessfully created {len(created_topics)} topics")
        
        # Small delay to ensure topics have been created before listing
        time.sleep(2)
        
        list_topics()
        print("\nKafka topics setup complete!")
        
    except Exception as e:
        print(f"Error creating Kafka topics: {e}")