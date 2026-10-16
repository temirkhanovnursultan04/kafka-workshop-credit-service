package kz.nurs.study.kafkaworkshopcreditservice.controller;

import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditRequestDto;
import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;
import kz.nurs.study.kafkaworkshopcreditservice.service.CreditAsyncService;
import kz.nurs.study.kafkaworkshopcreditservice.service.CreditSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditApiController {

    private final CreditSyncService creditSyncService;
    private final CreditAsyncService creditAsyncService;

    /** Синхронно: ждём, пока сервис уведомлений ответит по REST, и только потом отвечаем клиенту. */
    @PostMapping("/sync")
    public ResponseEntity<CreditResponseDto> sync(@RequestBody CreditRequestDto request) {
        return ResponseEntity.ok(creditSyncService.processCredit(request));
    }

    /** Асинхронно: кладём сообщение в Kafka и сразу отвечаем 202 Accepted — «принято в обработку». */
    @PostMapping("/async")
    public ResponseEntity<CreditResponseDto> async(@RequestBody CreditRequestDto request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(creditAsyncService.processCredit(request));
    }
}
