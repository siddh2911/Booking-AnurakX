package com.karunavilla.booking_system.controller;

import com.karunavilla.booking_system.model.BookingDTO;
import com.karunavilla.booking_system.model.BookingResponseDTO;
import com.karunavilla.booking_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping(path = "/saveBooking")
    public ResponseEntity<?> saveBooking(@RequestBody BookingDTO booking){
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
}
