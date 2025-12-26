package com.karunavilla.booking_system.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    private BigDecimal advanceAmount;
    private BigDecimal pendingAmount;
    private String methodAdvanceAmountPaid;
    private String methodPendingAmountPaid;
    private String type; // "Advance" or "Settlement"
    private Instant paymentDate;

    @Column(name = "additional_amount" ,columnDefinition = "LONGTEXT")
    private String additionalAmountJson;
}
