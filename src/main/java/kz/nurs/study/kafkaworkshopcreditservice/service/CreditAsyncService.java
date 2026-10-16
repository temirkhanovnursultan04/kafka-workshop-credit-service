package kz.nurs.study.kafkaworkshopcreditservice.service;

import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditRequestDto;
import kz.nurs.study.kafkaworkshopcreditservice.dto.CreditResponseDto;

public interface CreditAsyncService {

    CreditResponseDto processCredit(CreditRequestDto request);
}
