package com.karunavilla.booking_system.model;

import com.karunavilla.booking_system.Entity.Guest;
import com.karunavilla.booking_system.Entity.Payment;
import com.karunavilla.booking_system.Entity.Room;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingDetailsDTO {
    private UUID uuid;
    private Room room;
    private Guest guest;
    private List<Payment> payments;
    private double pendingBalance;
}
