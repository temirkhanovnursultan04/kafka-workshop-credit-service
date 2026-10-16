package kz.nurs.study.kafkaworkshopcreditservice.client;

import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;

public interface CreditRestClient {

    void sendCreditDecision(CreditResponseDto creditDecision);
}
