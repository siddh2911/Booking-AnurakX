package com.karunavilla.booking_system.repository;

import com.karunavilla.booking_system.Entity.Booking;
import com.karunavilla.booking_system.Entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    // New method for scheduled task
    @Query("SELECT b FROM Booking b JOIN b.room r WHERE b.checkOutDate < :currentTime AND b.status = 'CONFIRMED' AND r.status = 'BOOKED'")
    List<Booking> findExpiredBookingsWithBookedRooms(@Param("currentTime") Instant currentTime);

    @Query("SELECT b FROM Booking b WHERE " +
           "(b.checkInDate < :requestedCheckOut AND b.checkOutDate > :requestedCheckIn) " +
           "AND b.status = 'CONFIRMED'")
    List<Booking> findOverlappingBookings(@Param("requestedCheckIn") Instant requestedCheckIn,
                                          @Param("requestedCheckOut") Instant requestedCheckOut);

    @Query("SELECT b FROM Booking b WHERE b.room = :room AND " +
           "(b.checkInDate < :requestedCheckOut AND b.checkOutDate > :requestedCheckIn) " +
           "AND b.status = 'CONFIRMED'")
    List<Booking> findOverlappingBookingsForRoom(@Param("room") Room room,
                                                 @Param("requestedCheckIn") Instant requestedCheckIn,
                                                 @Param("requestedCheckOut") Instant requestedCheckOut);
}
