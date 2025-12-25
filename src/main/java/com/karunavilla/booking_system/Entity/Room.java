package com.karunavilla.booking_system.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roomNumber; // "101", "201", etc.
    private String roomName;
    private String type; // "Single", "Suite", etc.
    private BigDecimal pricePerNight;
    private String status; // "Available", "Dirty", etc.
}