package com.karunavilla.booking_system.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdditionalPay {
    private String category;
    private BigDecimal amount;
}
