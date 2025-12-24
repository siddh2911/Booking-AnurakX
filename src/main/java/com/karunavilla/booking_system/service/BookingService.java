package com.karunavilla.booking_system.service;

import com.karunavilla.booking_system.Entity.Booking;
import com.karunavilla.booking_system.Entity.Guest;
import com.karunavilla.booking_system.Entity.Room;
import com.karunavilla.booking_system.model.*;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        Instant checkInDate = bookingDTO.getCheckInDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant checkOutDate = bookingDTO.getCheckOutDate().atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookingsForRoom(room, checkInDate, checkOutDate);

        if (!overlappingBookings.isEmpty()) {
            throw new RuntimeException("Room " + bookingDTO.getRoomNo() + " is not available for the selected dates.");
        }

        Booking booking = new Booking();
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setBookingSource(bookingDTO.getBookingSource());
        booking.setInternalNotes(bookingDTO.getInternalNotes());
        booking.setAmountPerNight(bookingDTO.getNightlyRate()); // Set amountPerNight
        BigDecimal calculatedTotalAmount = bookingDTO.getNightlyRate().multiply(BigDecimal.valueOf(bookingDTO.getCheckOutDate().toEpochDay() - bookingDTO.getCheckInDate().toEpochDay()));
        if (bookingDTO.getAdditionalCharges() != null && !bookingDTO.getAdditionalCharges().isEmpty()) {
            calculatedTotalAmount = calculatedTotalAmount.add(bookingDTO.getAdditionalCharges().stream()
                    .map(AdditionalPay::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        booking.setTotalAmount(calculatedTotalAmount);
        booking.setStatus("CONFIRMED");
        booking.setPayments(new java.util.ArrayList<>());

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
            guestRepository.save(guest);
        }

        // Determine the proposed state of the booking
        Room proposedRoom = existingBooking.getRoom();
        if (bookingDTO.getRoomNo() != null && !bookingDTO.getRoomNo().equals(existingBooking.getRoom().getRoomNumber())) {
            proposedRoom = roomRepository.findByRoomNumber(bookingDTO.getRoomNo())
                    .orElseThrow(() -> new RuntimeException("New room not found: " + bookingDTO.getRoomNo()));
        }

        LocalDate proposedCheckInDate = existingBooking.getCheckInDate().atZone(ZoneOffset.UTC).toLocalDate();
        if (bookingDTO.getCheckInDate() != null) {
            proposedCheckInDate = bookingDTO.getCheckInDate();
        }

        LocalDate proposedCheckOutDate = existingBooking.getCheckOutDate().atZone(ZoneOffset.UTC).toLocalDate();
        if (bookingDTO.getCheckOutDate() != null) {
            proposedCheckOutDate = bookingDTO.getCheckOutDate();
        }

        // Check for conflicts if room or dates are changing
        if ((bookingDTO.getRoomNo() != null && !bookingDTO.getRoomNo().equals(existingBooking.getRoom().getRoomNumber())) ||
            bookingDTO.getCheckInDate() != null || bookingDTO.getCheckOutDate() != null) {

            Instant proposedCheckInInstant = proposedCheckInDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant proposedCheckOutInstant = proposedCheckOutDate.atStartOfDay().toInstant(ZoneOffset.UTC);

            List<Booking> overlapping = bookingRepository.findOverlappingBookingsForRoom(proposedRoom, proposedCheckInInstant, proposedCheckOutInstant);

            // A booking can't overlap with itself.
            final Long currentId = existingBooking.getId();
            overlapping.removeIf(b -> b.getId().equals(currentId));

            if (!overlapping.isEmpty()) {
                throw new RuntimeException("Room is not available for the specified dates.");
            }
        }
        BigDecimal calculatedTotalAmount = bookingDTO.getNightlyRate().multiply(BigDecimal.valueOf(bookingDTO.getCheckOutDate().toEpochDay() - bookingDTO.getCheckInDate().toEpochDay()));
        if (bookingDTO.getAdditionalCharges() != null && !bookingDTO.getAdditionalCharges().isEmpty()) {
            calculatedTotalAmount = calculatedTotalAmount.add(bookingDTO.getAdditionalCharges().stream()
                    .map(AdditionalPay::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        existingBooking.setTotalAmount(calculatedTotalAmount);

        // Correctly handle payment updates
        if (bookingDTO.getAdvanceAmount() != null) {
            Optional<Payment> existingPaymentOpt = existingBooking.getPayments().stream()
                    .filter(p -> "ADVANCE".equals(p.getType()))
                    .findFirst();

            if (existingPaymentOpt.isPresent()) {
                Payment advancePayment = existingPaymentOpt.get();
                advancePayment.setAdvanceAmount(bookingDTO.getAdvanceAmount());
                advancePayment.setPendingAmount(calculatedTotalAmount.subtract(bookingDTO.getAdvanceAmount()));
                if (bookingDTO.getPaymentMethod() != null) {
                    advancePayment.setMethodAdvanceAmountPaid(bookingDTO.getPaymentMethod());
                }
            } else {
                Payment newAdvancePayment = new Payment();
                newAdvancePayment.setAdvanceAmount(bookingDTO.getAdvanceAmount());
                newAdvancePayment.setMethodAdvanceAmountPaid(bookingDTO.getPaymentMethod());
                newAdvancePayment.setPendingAmount(calculatedTotalAmount.subtract(bookingDTO.getAdvanceAmount()));
                newAdvancePayment.setType("ADVANCE");
                newAdvancePayment.setBooking(existingBooking);
                existingBooking.getPayments().add(newAdvancePayment);
            }
        }

        // If no conflicts, update the booking
        Room oldRoom = existingBooking.getRoom();
        if(!oldRoom.equals(proposedRoom)){
            oldRoom.setStatus("AVAILABLE");
            roomRepository.save(oldRoom);
            proposedRoom.setStatus("BOOKED");
            roomRepository.save(proposedRoom);
        }
        existingBooking.setRoom(proposedRoom);
        existingBooking.setCheckInDate(proposedCheckInDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        existingBooking.setCheckOutDate(proposedCheckOutDate.atStartOfDay().toInstant(ZoneOffset.UTC));


        if (bookingDTO.getBookingSource() != null) existingBooking.setBookingSource(bookingDTO.getBookingSource());
        if (bookingDTO.getInternalNotes() != null) existingBooking.setInternalNotes(bookingDTO.getInternalNotes());
        // Recalculate total amount if nightly rate or dates change
        if (bookingDTO.getNightlyRate() != null || bookingDTO.getCheckInDate() != null || bookingDTO.getCheckOutDate() != null) {
            BigDecimal nightlyRate = bookingDTO.getNightlyRate() != null ? bookingDTO.getNightlyRate() : existingBooking.getAmountPerNight(); // Use existing amountPerNight if nightlyRate not provided in DTO
            long days = proposedCheckOutDate.toEpochDay() - proposedCheckInDate.toEpochDay();
            existingBooking.setTotalAmount(nightlyRate.multiply(BigDecimal.valueOf(days)));
            existingBooking.setAmountPerNight(nightlyRate); // Update amountPerNight as well
        }

        existingBooking = bookingRepository.save(existingBooking);

        return getBookingDetailsById(existingBooking.getId()); // Return DTO of updated booking
    }

    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        Guest guest = booking.getGuest(); // Get the associated guest
        bookingRepository.delete(booking); // Delete the booking

        // Check if the guest has any other bookings
        if (guestRepository.countBookingsByGuestId(guest.getId()) == 0) {
            guestRepository.delete(guest); // If no other bookings, delete the guest
        }
    }

    public List<RoomAvailabilityResponse> getRoomsAvailable(RoomAvailabilityRequest request) {
        Instant requestedCheckIn = request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant requestedCheckOut = request.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Room> allRooms = roomRepository.findAll();

        Set<Long> bookedRoomIds = bookingRepository.findOverlappingBookings(requestedCheckIn, requestedCheckOut)
                .stream()
                .map(booking -> booking.getRoom().getId())
                .collect(Collectors.toSet());

        return allRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .map(RoomAvailabilityResponse::new) // Using the constructor for mapping
                .collect(Collectors.toList());
    }
}



