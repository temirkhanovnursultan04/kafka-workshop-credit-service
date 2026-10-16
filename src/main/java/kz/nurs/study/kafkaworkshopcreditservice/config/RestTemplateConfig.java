package kz.nurs.study.kafkaworkshopcreditservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;

/**
 * RestTemplate для синхронных вызовов notification-service.
 * Базовый адрес берём из app.notification-service.url — в коде пишем только путь.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate notificationRestTemplate(
            @Value("${app.notification-service.url}") String notificationServiceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));  // сервис выключен — узнаем быстро
        requestFactory.setReadTimeout(Duration.ofSeconds(10));    // сервис «думает» — ждём до 10 с

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(notificationServiceUrl));
        return restTemplate;
    }
}
