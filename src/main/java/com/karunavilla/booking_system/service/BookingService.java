package com.karunavilla.booking_system.service;

import com.karunavilla.booking_system.Entity.Booking;
import com.karunavilla.booking_system.Entity.Guest;
import com.karunavilla.booking_system.Entity.Room;
import com.karunavilla.booking_system.model.BookingDTO;
import com.karunavilla.booking_system.model.BookingResponseDTO;
import com.karunavilla.booking_system.repository.BookingRepository;
import com.karunavilla.booking_system.repository.GuestRepository;
import com.karunavilla.booking_system.repository.RoomRepository;
import com.karunavilla.booking_system.Entity.Payment;
import java.math.BigDecimal;

import com.karunavilla.booking_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public Booking createBooking(BookingDTO bookingDTO) {
        Guest guest = new Guest();
        guest.setFullName(bookingDTO.getFullName());
        guest.setEmail(bookingDTO.getEmailId());
        guest.setMobileNumber(bookingDTO.getMobileNumber());
        guest = guestRepository.save(guest);

        Room room = roomRepository.findByRoomNumber(bookingDTO.getRoomNo())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Booking booking = new Booking();
        BigDecimal calculatedTotalAmount = null;
        if (room.getStatus() == null || !room.getStatus().equalsIgnoreCase("BOOKED")) { // Added null check
            booking.setGuest(guest);
            booking.setRoom(room);
            booking.setCheckInDate(bookingDTO.getCheckInDate().atStartOfDay().toInstant(ZoneOffset.UTC));
            booking.setCheckOutDate(bookingDTO.getCheckOutDate().atStartOfDay().toInstant(ZoneOffset.UTC));
            booking.setBookingSource(bookingDTO.getBookingSource());
            booking.setInternalNotes(bookingDTO.getInternalNotes());
            booking.setAmountPerNight(bookingDTO.getNightlyRate()); // Set amountPerNight
            calculatedTotalAmount = bookingDTO.getNightlyRate().multiply(BigDecimal.valueOf(bookingDTO.getCheckOutDate().toEpochDay() - bookingDTO.getCheckInDate().toEpochDay()));
            booking.setTotalAmount(calculatedTotalAmount);
            booking.setStatus("CONFIRMED");
            booking.setPayments(new java.util.ArrayList<>());

            room.setStatus("BOOKED"); // Set room status to BOOKED
            roomRepository.save(room); // Save the updated room status
        } else {
            throw new RuntimeException("Room " + bookingDTO.getRoomNo() + " is already booked."); // Handle case where room is already booked
        }

        if (bookingDTO.getAdvanceAmount() != null && calculatedTotalAmount != null) {
            Payment advancePayment = new Payment();
            advancePayment.setAdvanceAmount(bookingDTO.getAdvanceAmount());
            advancePayment.setMethodAdvanceAmountPaid(bookingDTO.getPaymentMethod());
            advancePayment.setPendingAmount(calculatedTotalAmount.subtract(bookingDTO.getAdvanceAmount()));
            advancePayment.setType("ADVANCE");
            advancePayment.setBooking(booking);
            booking.getPayments().add(advancePayment);
        }

        booking = bookingRepository.save(booking);
        return booking;
    }

    public void updateRoomStatusAfterCheckout(Booking booking) {
        if (booking.getCheckOutDate().isBefore(Instant.now())) {
            Room room = booking.getRoom();
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
            // TODO: Implement a scheduler to call this method for expired bookings
        }
    }

    public List<BookingResponseDTO> getAllBookingDetails() {
        List<Booking> bookings = bookingRepository.findAll();
        List<BookingResponseDTO> bookingResponseDTOs = new java.util.ArrayList<>();

        for (Booking booking : bookings) {
            BookingResponseDTO bookingResponseDTO = new BookingResponseDTO();
            bookingResponseDTO.setId(booking.getId()); // Assuming Booking entity has a getId() for UUID
            bookingResponseDTO.setGuest(booking.getGuest().getFullName());
            bookingResponseDTO.setBookingSource(booking.getBookingSource());
            bookingResponseDTO.setRoom(booking.getRoom().getRoomNumber());
            bookingResponseDTO.setCheckInDate(booking.getCheckInDate());
            bookingResponseDTO.setCheckOutDate(booking.getCheckOutDate());
            bookingResponseDTO.setStatus(booking.getStatus());
            bookingResponseDTO.setContactNumber(booking.getGuest().getMobileNumber());

            double totalPaid = booking.getPayments().stream()
                    .mapToDouble(payment -> payment.getAdvanceAmount() != null ? payment.getAdvanceAmount().doubleValue() : 0.0)
                    .sum();

            bookingResponseDTO.setTotalPaid(BigDecimal.valueOf(totalPaid));
            BigDecimal pendingBalance = booking.getTotalAmount().subtract(BigDecimal.valueOf(totalPaid));
            bookingResponseDTO.setBalance(pendingBalance);
            bookingResponseDTOs.add(bookingResponseDTO);
        }
        return bookingResponseDTOs;
    }

    public BookingDTO getBookingDetailsById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setFullName(booking.getGuest().getFullName());
        bookingDTO.setEmailId(booking.getGuest().getEmail());
        bookingDTO.setMobileNumber(booking.getGuest().getMobileNumber());
        bookingDTO.setRoomNo(booking.getRoom().getRoomNumber());
        bookingDTO.setCheckInDate(booking.getCheckInDate().atZone(ZoneOffset.UTC).toLocalDate());
        bookingDTO.setCheckOutDate(booking.getCheckOutDate().atZone(ZoneOffset.UTC).toLocalDate());
        bookingDTO.setBookingSource(booking.getBookingSource());
        bookingDTO.setInternalNotes(booking.getInternalNotes());
        bookingDTO.setNightlyRate(booking.getAmountPerNight());
        bookingDTO.setTotalAmount(booking.getTotalAmount());

        // Find advance amount and payment method from payments list
        booking.getPayments().stream()
                .filter(payment -> "ADVANCE".equals(payment.getType()))
                .findFirst()
                .ifPresent(payment -> {
                    bookingDTO.setAdvanceAmount(payment.getAdvanceAmount());
                    bookingDTO.setPaymentMethod(payment.getMethodAdvanceAmountPaid());
                });

        return bookingDTO;
    }

    @Transactional
    public BookingDTO updateBooking(Long bookingId, BookingDTO bookingDTO) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        // Update Guest details if provided in DTO
        if (bookingDTO.getFullName() != null || bookingDTO.getEmailId() != null || bookingDTO.getMobileNumber() != null) {
            Guest guest = existingBooking.getGuest();
            if (bookingDTO.getFullName() != null) guest.setFullName(bookingDTO.getFullName());
            if (bookingDTO.getEmailId() != null) guest.setEmail(bookingDTO.getEmailId());
            if (bookingDTO.getMobileNumber() != null) guest.setMobileNumber(bookingDTO.getMobileNumber());
            guestRepository.save(guest); // Save updated guest details
        }

        // Update Room if room number is provided and different
        if (bookingDTO.getRoomNo() != null && !bookingDTO.getRoomNo().equals(existingBooking.getRoom().getRoomNumber())) {
            Room newRoom = roomRepository.findByRoomNumber(bookingDTO.getRoomNo())
                    .orElseThrow(() -> new RuntimeException("New room not found: " + bookingDTO.getRoomNo()));
            // Optionally, mark old room as AVAILABLE if it's currently BOOKED by this booking
            if (existingBooking.getRoom().getStatus().equalsIgnoreCase("BOOKED")) {
                existingBooking.getRoom().setStatus("AVAILABLE");
                roomRepository.save(existingBooking.getRoom());
            }
            newRoom.setStatus("BOOKED");
            roomRepository.save(newRoom);
            existingBooking.setRoom(newRoom);
        }

        // Update Booking details
        if (bookingDTO.getCheckInDate() != null) existingBooking.setCheckInDate(bookingDTO.getCheckInDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        if (bookingDTO.getCheckOutDate() != null) existingBooking.setCheckOutDate(bookingDTO.getCheckOutDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        if (bookingDTO.getBookingSource() != null) existingBooking.setBookingSource(bookingDTO.getBookingSource());
        if (bookingDTO.getInternalNotes() != null) existingBooking.setInternalNotes(bookingDTO.getInternalNotes());
        // Recalculate total amount if nightly rate or dates change
        if (bookingDTO.getNightlyRate() != null || bookingDTO.getCheckInDate() != null || bookingDTO.getCheckOutDate() != null) {
            BigDecimal nightlyRate = bookingDTO.getNightlyRate() != null ? bookingDTO.getNightlyRate() : existingBooking.getAmountPerNight(); // Use existing amountPerNight if nightlyRate not provided in DTO
            long days = (bookingDTO.getCheckOutDate() != null ? bookingDTO.getCheckOutDate().toEpochDay() : existingBooking.getCheckOutDate().atZone(ZoneOffset.UTC).toLocalDate().toEpochDay())
                        - (bookingDTO.getCheckInDate() != null ? bookingDTO.getCheckInDate().toEpochDay() : existingBooking.getCheckInDate().atZone(ZoneOffset.UTC).toLocalDate().toEpochDay());
            existingBooking.setTotalAmount(nightlyRate.multiply(BigDecimal.valueOf(days)));
            existingBooking.setAmountPerNight(nightlyRate); // Update amountPerNight as well
        }

        
        existingBooking = bookingRepository.save(existingBooking);

        return getBookingDetailsById(existingBooking.getId()); // Return DTO of updated booking
    }

    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
        
        // Optionally, free up the room if it was booked by this booking
        if (booking.getRoom().getStatus().equalsIgnoreCase("BOOKED") && booking.getRoom().equals(booking.getRoom())) { // Check if this booking holds the room
             booking.getRoom().setStatus("AVAILABLE");
             roomRepository.save(booking.getRoom());
        }
        bookingRepository.delete(booking);
    }
}

