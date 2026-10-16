package kz.nurs.study.kafkaworkshopcreditservice.service;

import kz.nurs.study.kafkaworkshopcreditservice.client.CreditKafkaClient;
import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditRequestDto;
import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * АСИНХРОННЫЙ сценарий: посчитали — положили сообщение в Kafka — СРАЗУ вернули клиенту.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditAsyncServiceImpl implements CreditAsyncService {

    private static final BigDecimal INTEREST = new BigDecimal("1.20"); // +20% к сумме кредита
    private static final BigDecimal TWO = new BigDecimal("2");

    private final CreditKafkaClient creditKafkaClient;

    @Override
    public CreditResponseDto processCredit(CreditRequestDto request) {
        long start = System.currentTimeMillis();

        // 1. Сколько вернуть банку: сумма + 20%
        BigDecimal totalToReturn = request.getCreditSum().multiply(INTEREST);

        // 2. Ежемесячный платёж: делим на срок, копейки округляем вверх —
        //    чтобы за последний месяц сумма точно была выплачена полностью
        BigDecimal monthlyPayment = totalToReturn.divide(
                BigDecimal.valueOf(request.getCreditPeriodInMonths()), 2, RoundingMode.UP);

        // 3. Решение: платёж должен укладываться в половину зарплаты
        BigDecimal halfSalary = request.getSalary().divide(TWO, 2, RoundingMode.DOWN);
        boolean isApproved = monthlyPayment.compareTo(halfSalary) <= 0;

        CreditResponseDto response = CreditResponseDto.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .creditSum(request.getCreditSum())
                .creditPeriodInMonths(request.getCreditPeriodInMonths())
                .salary(request.getSalary())
                .isApproved(isApproved)
                .monthlyPayment(monthlyPayment)
                .build();

        log.info("[ASYNC] {} {}: approved={}, monthlyPayment={}",
                response.getName(), response.getSurname(), isApproved, monthlyPayment);

        // 4. Кладём сообщение в Kafka и не ждём — сервис уведомлений заберёт его сам
        creditKafkaClient.sendCreditDecision(response);

        log.info("[ASYNC] Клиенту ответили через {} мс", System.currentTimeMillis() - start);
        return response;
    }
}
