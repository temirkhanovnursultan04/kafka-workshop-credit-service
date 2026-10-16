package kz.nurs.study.kafkaworkshopcreditservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Решение по кредиту — его получает клиент и его же мы отправляем в сервис уведомлений.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponseDto {

    private String name;
    private String surname;
    private BigDecimal creditSum;
    private Integer creditPeriodInMonths;
    private BigDecimal salary;
    private Boolean isApproved;
    private BigDecimal monthlyPayment;
}
