package com.karunavilla.booking_system.service;

import com.karunavilla.booking_system.Entity.Booking;
import com.karunavilla.booking_system.Entity.Guest;
import com.karunavilla.booking_system.Entity.Room;
import com.karunavilla.booking_system.model.BookingDTO;
import com.karunavilla.booking_system.repository.BookingRepository;
import com.karunavilla.booking_system.repository.GuestRepository;
import com.karunavilla.booking_system.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private RoomRepository roomRepository;

    @Test
    public void testCreateBooking() {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setFullName("Test User");
        bookingDTO.setEmailId("test@example.com");
        bookingDTO.setMobileNumber("1234567890");
        bookingDTO.setCheckInDate(LocalDate.now());
        bookingDTO.setCheckOutDate(LocalDate.now().plusDays(2));
        bookingDTO.setRoomNo("101");
        bookingDTO.setNightlyRate(new BigDecimal("100"));
        bookingDTO.setBookingSource("WEB");
        bookingDTO.setAdvanceAmount(new BigDecimal("50"));
        bookingDTO.setPaymentMethod("CASH");
        bookingDTO.setInternalNotes("Test booking");

        Guest guest = new Guest();
        guest.setId(1L);
        guest.setFullName(bookingDTO.getFullName());
        guest.setEmail(bookingDTO.getEmailId());
        guest.setMobileNumber(bookingDTO.getMobileNumber());

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setStatus("AVAILABLE"); // Set a status for the room

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDTO.getCheckInDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setCheckOutDate(bookingDTO.getCheckOutDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setStatus("CONFIRMED");
        booking.setTotalAmount(new BigDecimal("200")); // Added to avoid NullPointerException in testGetBookingDetailsById

        when(guestRepository.save(any(Guest.class))).thenReturn(guest);
        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.createBooking(bookingDTO);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(guest.getFullName(), result.getGuest().getFullName());
    }

    @Test
    public void testGetBookingDetailsById() {
        // Mock data
        Guest guest = new Guest();
        guest.setFullName("Test Guest");
        Room room = new Room();
        room.setRoomNumber("101");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setBookingSource("WEB");
        booking.setCheckInDate(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setCheckOutDate(LocalDate.now().plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setStatus("CONFIRMED");
        booking.setTotalAmount(new BigDecimal("200"));
        booking.setPayments(new java.util.ArrayList<>());

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // Call the service method
        BookingDTO result = bookingService.getBookingDetailsById(1L);

        // Assertions
        assertNotNull(result);
        assertEquals(booking.getGuest().getFullName(), result.getFullName());
        assertEquals(booking.getGuest().getEmail(), result.getEmailId());
        assertEquals(booking.getGuest().getMobileNumber(), result.getMobileNumber());
        assertEquals(room.getRoomNumber(), result.getRoomNo());
        assertEquals(booking.getBookingSource(), result.getBookingSource());
        assertEquals(booking.getCheckInDate().atZone(ZoneOffset.UTC).toLocalDate(), result.getCheckInDate());
        assertEquals(booking.getCheckOutDate().atZone(ZoneOffset.UTC).toLocalDate(), result.getCheckOutDate());
        assertEquals(booking.getAmountPerNight(), result.getNightlyRate());
    }

    @Test
    public void testGetBookingDetailsById_NotFound() {
        when(bookingRepository.findById(2L)).thenReturn(Optional.empty());

        // Assert that a RuntimeException is thrown when booking is not found
        assertThrows(RuntimeException.class, () -> bookingService.getBookingDetailsById(2L));
    }

    @Test
    public void testUpdateBooking_GuestDetails() {
        // Initial booking data
        Guest initialGuest = new Guest();
        initialGuest.setId(1L);
        initialGuest.setFullName("Old Guest");
        initialGuest.setEmail("old@example.com");
        initialGuest.setMobileNumber("1111111111");

        Room initialRoom = new Room();
        initialRoom.setId(1L);
        initialRoom.setRoomNumber("101");
        initialRoom.setStatus("BOOKED");

        Booking initialBooking = new Booking();
        initialBooking.setId(1L);
        initialBooking.setGuest(initialGuest);
        initialBooking.setRoom(initialRoom);
        initialBooking.setCheckInDate(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
        initialBooking.setCheckOutDate(LocalDate.now().plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));
        initialBooking.setBookingSource("WEB");
        initialBooking.setInternalNotes("Old notes");
        initialBooking.setAmountPerNight(new BigDecimal("100"));
        initialBooking.setTotalAmount(new BigDecimal("200"));
        initialBooking.setStatus("CONFIRMED");
        initialBooking.setPayments(new java.util.ArrayList<>());

        // DTO with updated guest details
        BookingDTO updatedBookingDTO = new BookingDTO();
        updatedBookingDTO.setFullName("New Guest");
        updatedBookingDTO.setEmailId("new@example.com");

        // Updated booking data that should be returned by findById after the update operation
        Guest updatedGuest = new Guest();
        updatedGuest.setId(1L);
        updatedGuest.setFullName("New Guest");
        updatedGuest.setEmail("new@example.com");
        updatedGuest.setMobileNumber("1111111111"); // Mobile number remains same as not updated

        Booking updatedBooking = new Booking();
        updatedBooking.setId(1L);
        updatedBooking.setGuest(updatedGuest);
        updatedBooking.setRoom(initialRoom);
        updatedBooking.setCheckInDate(initialBooking.getCheckInDate());
        updatedBooking.setCheckOutDate(initialBooking.getCheckOutDate());
        updatedBooking.setBookingSource(initialBooking.getBookingSource());
        updatedBooking.setInternalNotes(initialBooking.getInternalNotes());
        updatedBooking.setAmountPerNight(initialBooking.getAmountPerNight());
        updatedBooking.setTotalAmount(initialBooking.getTotalAmount());
        updatedBooking.setStatus(initialBooking.getStatus());
        updatedBooking.setPayments(initialBooking.getPayments());


        // Mock repository calls
        when(bookingRepository.findById(1L))
            .thenReturn(Optional.of(initialBooking)) // First call from updateBooking method
            .thenReturn(Optional.of(updatedBooking)); // Second call from getBookingDetailsById method

        when(guestRepository.save(any(Guest.class))).thenReturn(updatedGuest);
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);

        // Call the service method
        BookingDTO result = bookingService.updateBooking(1L, updatedBookingDTO);

        // Assertions
        assertNotNull(result);
        assertEquals(updatedBookingDTO.getFullName(), result.getFullName());
        assertEquals(initialRoom.getRoomNumber(), result.getRoomNo());
    }

    @Test
    public void testUpdateBooking_RoomChange() {
        // Initial booking data
        Guest initialGuest = new Guest();
        initialGuest.setId(1L);
        initialGuest.setFullName("Test Guest");

        Room oldRoom = new Room();
        oldRoom.setId(1L);
        oldRoom.setRoomNumber("101");
        oldRoom.setStatus("BOOKED");

        Room newRoom = new Room();
        newRoom.setId(2L);
        newRoom.setRoomNumber("202");
        newRoom.setStatus("AVAILABLE");

        Booking initialBooking = new Booking();
        initialBooking.setId(1L);
        initialBooking.setGuest(initialGuest);
        initialBooking.setRoom(oldRoom);
        initialBooking.setCheckInDate(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
        initialBooking.setCheckOutDate(LocalDate.now().plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));
        initialBooking.setBookingSource("WEB");
        initialBooking.setInternalNotes("Old notes");
        initialBooking.setAmountPerNight(new BigDecimal("100"));
        initialBooking.setTotalAmount(new BigDecimal("200"));
        initialBooking.setStatus("CONFIRMED");
        initialBooking.setPayments(new java.util.ArrayList<>());

        // DTO with updated room number
        BookingDTO updatedBookingDTO = new BookingDTO();
        updatedBookingDTO.setRoomNo("202");

        // Updated booking data that should be returned by findById after the update operation
        Booking updatedBooking = new Booking();
        updatedBooking.setId(1L);
        updatedBooking.setGuest(initialGuest);
        updatedBooking.setRoom(newRoom); // Room should be updated to newRoom
        updatedBooking.setCheckInDate(initialBooking.getCheckInDate());
        updatedBooking.setCheckOutDate(initialBooking.getCheckOutDate());
        updatedBooking.setBookingSource(initialBooking.getBookingSource());
        updatedBooking.setInternalNotes(initialBooking.getInternalNotes());
        updatedBooking.setAmountPerNight(initialBooking.getAmountPerNight());
        updatedBooking.setTotalAmount(initialBooking.getTotalAmount());
        updatedBooking.setStatus(initialBooking.getStatus());
        updatedBooking.setPayments(initialBooking.getPayments());


        // Mock repository calls
        when(bookingRepository.findById(1L))
            .thenReturn(Optional.of(initialBooking)) // First call from updateBooking method
            .thenReturn(Optional.of(updatedBooking)); // Second call from getBookingDetailsById method

        when(roomRepository.findByRoomNumber("202")).thenReturn(Optional.of(newRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(oldRoom, newRoom); // Save old room as AVAILABLE, then new room as BOOKED
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);

        // Call the service method
        BookingDTO result = bookingService.updateBooking(1L, updatedBookingDTO);

        // Assertions
        assertNotNull(result);
        assertEquals(updatedBookingDTO.getRoomNo(), result.getRoomNo());
        assertEquals("AVAILABLE", oldRoom.getStatus()); // Old room should be available
        assertEquals("BOOKED", newRoom.getStatus()); // New room should be booked
    }
}