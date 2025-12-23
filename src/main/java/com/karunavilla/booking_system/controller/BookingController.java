package com.karunavilla.booking_system.controller;

import com.karunavilla.booking_system.Entity.Room;
import com.karunavilla.booking_system.model.BookingDTO;
import com.karunavilla.booking_system.model.BookingResponseDTO;
import com.karunavilla.booking_system.model.RoomAvailabilityRequest;
import com.karunavilla.booking_system.model.RoomAvailabilityResponse; // Added import
import com.karunavilla.booking_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping(path = "/saveBooking")
    public ResponseEntity<?> saveBooking(@Valid @RequestBody BookingDTO booking){
        bookingService.createBooking(booking);
        return ResponseEntity.ok("Booking created successfully");
    }

    @GetMapping(path = "/allBooking")
    public ResponseEntity<?> getBookingDetails() {
        List<BookingResponseDTO> bookingDetails = bookingService.getAllBookingDetails();
        return ResponseEntity.ok(bookingDetails);
    }

    @GetMapping(path = "/bookings/{id}")
    public ResponseEntity<?> getBookingDetailsById(@PathVariable Long id) {
        BookingDTO bookingDetails = bookingService.getBookingDetailsById(id);
        return ResponseEntity.ok(bookingDetails);
    }

    @PutMapping(path = "/bookings/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody BookingDTO bookingDTO) {
        BookingDTO updatedBooking = bookingService.updateBooking(id, bookingDTO);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping(path = "/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomAvailabilityResponse>> getRoomsAvailable(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RoomAvailabilityRequest request = new RoomAvailabilityRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        List<RoomAvailabilityResponse> availableRooms = bookingService.getRoomsAvailable(request);
        return ResponseEntity.ok(availableRooms);
    }
}

