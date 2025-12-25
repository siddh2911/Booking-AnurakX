package com.karunavilla.booking_system.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@Table(name = "bookings")
public class Booking {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @ManyToOne
       @JoinColumn(name = "guest_id", nullable = false)
       private Guest guest;

       @ManyToOne
       @JoinColumn(name = "room_id", nullable = false)
       private Room room;

       private Instant checkInDate;
       private Instant checkOutDate;
       private String bookingSource;
       private String internalNotes;
       private BigDecimal amountPerNight;
       private BigDecimal totalAmount;
       private String roomName;
       private String status;

       @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
       private List<Payment> payments;

   }
