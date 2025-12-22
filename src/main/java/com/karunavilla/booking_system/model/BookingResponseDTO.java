package com.karunavilla.booking_system.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class BookingResponseDTO {
    private Long id;
    private String guest;
    private String bookingSource;
    private String room;
    private Instant checkInDate;
    private Instant checkOutDate;
    private String status;
    private BigDecimal balance;
    private BigDecimal totalPaid;
    private String contactNumber;
    private String actions;

}