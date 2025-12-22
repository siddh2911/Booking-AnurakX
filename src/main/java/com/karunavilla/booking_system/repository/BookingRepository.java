package com.karunavilla.booking_system.repository;

import com.karunavilla.booking_system.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

}
