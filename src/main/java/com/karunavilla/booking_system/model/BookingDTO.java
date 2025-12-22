package com.karunavilla.booking_system.model;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingDTO {

    @NotBlank(message = "Guest full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String emailId;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^(?:\\+91)?[0-9]{10}$", message = "Please enter a valid 10-digit Indian mobile number, optionally starting with +91")
    private String mobileNumber;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @NotBlank(message = "Room number is required")
    private String roomNo;

    @NotNull(message = "Nightly rate is required")
    @DecimalMin(value = "0.01", message = "Nightly rate must be positive")
    private BigDecimal nightlyRate;

    @NotNull(message = "total rate is required")
    @DecimalMin(value = "0.01", message = "Total Amount must be positive")
    private BigDecimal totalAmount;

    @NotBlank(message = "Booking source is required")
    private String bookingSource;

    @NotNull(message = "Advance amount is required")
    @DecimalMin(value = "0.00", message = "Advance amount cannot be negative")
    private BigDecimal advanceAmount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String internalNotes;

}
