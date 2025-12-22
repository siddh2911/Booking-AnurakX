package com.karunavilla.booking_system.repository;

import com.karunavilla.booking_system.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
