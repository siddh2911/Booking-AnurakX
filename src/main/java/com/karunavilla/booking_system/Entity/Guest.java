package com.karunavilla.booking_system.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "guests")
@Data
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
}