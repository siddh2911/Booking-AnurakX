package com.karunavilla.booking_system.repository;

import com.karunavilla.booking_system.Entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.guest.id = :guestId")
    Long countBookingsByGuestId(@Param("guestId") Long guestId);
}
