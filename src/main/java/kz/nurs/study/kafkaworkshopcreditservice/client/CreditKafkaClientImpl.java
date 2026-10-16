package kz.nurs.study.kafkaworkshopcreditservice.client;

import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Отправка через Kafka: кладём сообщение в топик и НЕ ждём, кто и когда его прочитает.
 */
@Slf4j
@Component
public class CreditKafkaClientImpl implements CreditKafkaClient {

    private final KafkaTemplate<String, CreditResponseDto> kafkaTemplate;
    private final String topic;

    public CreditKafkaClientImpl(KafkaTemplate<String, CreditResponseDto> kafkaTemplate,
                                 @Value("${app.kafka-topics.credit-topic.name}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void sendCreditDecision(CreditResponseDto creditDecision) {
        log.info("[KAFKA] Кладём решение в топик '{}'", topic);
        kafkaTemplate.send(topic, creditDecision.getSurname(), creditDecision)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[KAFKA] Не удалось отправить сообщение", ex);
                    } else {
                        log.info("[KAFKA] Сообщение в топике: partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
