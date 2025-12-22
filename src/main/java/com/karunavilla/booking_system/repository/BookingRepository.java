package com.karunavilla.booking_system.repository;

import com.karunavilla.booking_system.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    // New method for scheduled task
    @Query("SELECT b FROM Booking b JOIN b.room r WHERE b.checkOutDate < :currentTime AND b.status = 'CONFIRMED' AND r.status = 'BOOKED'")
    List<Booking> findExpiredBookingsWithBookedRooms(@Param("currentTime") Instant currentTime);

    @Query("SELECT b FROM Booking b WHERE " +
           "(b.checkInDate < :requestedCheckOut AND b.checkOutDate > :requestedCheckIn)")
    List<Booking> findOverlappingBookings(@Param("requestedCheckIn") Instant requestedCheckIn,
                                          @Param("requestedCheckOut") Instant requestedCheckOut);
}
