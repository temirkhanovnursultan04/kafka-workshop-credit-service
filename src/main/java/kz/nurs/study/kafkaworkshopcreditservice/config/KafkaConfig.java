package kz.nurs.study.kafkaworkshopcreditservice.config;

import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka для credit-service. Сервис только ПРОИЗВОДИТ события (producer) — consumer'а здесь нет.
 * Все настройки берём из application.yaml (spring.kafka.* и app.kafka-topics.*).
 */
@Configuration
public class KafkaConfig {

    /**
     * Фабрика producer'ов: bootstrap-servers, acks, lz4, batch-size, linger.ms,
     * idempotence и т.д. приходят из блока spring.kafka.producer в application.yaml.
     */
    @Bean
    public ProducerFactory<String, CreditResponseDto> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        // Ключ — строка (фамилия клиента), значение — DTO в виде JSON
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /** KafkaTemplate — через него CreditKafkaClientImpl отправляет события в топик. */
    @Bean
    public KafkaTemplate<String, CreditResponseDto> kafkaTemplate(
            ProducerFactory<String, CreditResponseDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Топик для решений по кредитам. Создаётся при старте приложения, если его ещё нет.
     * replicas=3 и min.insync.replicas=2 требуют кластер минимум из 3 брокеров.
     */
    @Bean
    public NewTopic creditTopic(
            @Value("${app.kafka-topics.credit-topic.name}") String name,
            @Value("${app.kafka-topics.credit-topic.partitions}") int partitions,
            @Value("${app.kafka-topics.credit-topic.replicas}") int replicas,
            @Value("${app.kafka-topics.credit-topic.min-in-sync-replicas}") String minInSyncReplicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInSyncReplicas)
                .build();
    }
}
