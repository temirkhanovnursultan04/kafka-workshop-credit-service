package kz.nurs.study.kafkaworkshopcreditservice.client;

import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;

public interface CreditKafkaClient {

    void sendCreditDecision(CreditResponseDto creditDecision);
}
