package kz.nurs.study.kafkaworkshopcreditservice.client;

import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Отправка по REST: HTTP POST в сервис уведомлений. Поток ЖДЁТ ответа.
 * Базовый адрес и таймауты настроены в RestTemplateConfig.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditRestClientImpl implements CreditRestClient {

    private final RestTemplate notificationRestTemplate;

    @Override
    public void sendCreditDecision(CreditResponseDto creditDecision) {
        log.info("[REST] Отправляем решение в сервис уведомлений и ждём ответа...");
        String answer = notificationRestTemplate.postForObject(
                "/api/notifications", creditDecision, String.class);
        log.info("[REST] Сервис уведомлений ответил: {}", answer);
    }
}
