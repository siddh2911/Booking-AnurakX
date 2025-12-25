package com.karunavilla.booking_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityResponse {
    private Long id;
    private String roomNumber;
    private String type;
    private BigDecimal pricePerNight;
    private String roomName;
    private String status; // e.g., "AVAILABLE"

    // Constructor to map from Room entity
    public RoomAvailabilityResponse(com.karunavilla.booking_system.Entity.Room room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.type = room.getType();
        this.pricePerNight = room.getPricePerNight();
        this.status = room.getStatus();
    }
}
