package com.karunavilla.booking_system.repository;

import com.karunavilla.booking_system.Entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

}
