package kz.nurs.study.kafkaworkshopcreditservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Заявка на кредит — то, что присылает клиент (браузер или Postman).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequestDto {

    private String name;
    private String surname;
    private BigDecimal creditSum;
    private Integer creditPeriodInMonths;
    private BigDecimal salary;
}
