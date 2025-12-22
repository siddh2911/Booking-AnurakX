package com.karunavilla.booking_system.service;

import com.karunavilla.booking_system.Entity.Booking;
import com.karunavilla.booking_system.Entity.Guest;
import com.karunavilla.booking_system.Entity.Room;
import com.karunavilla.booking_system.model.BookingDTO;
import com.karunavilla.booking_system.model.RoomAvailabilityRequest;
import com.karunavilla.booking_system.model.RoomAvailabilityResponse;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Test
    public void testGetRoomsAvailable_noBookings() {
        LocalDate startDate = LocalDate.of(2024, 10, 20);
        LocalDate endDate = LocalDate.of(2024, 10, 25);
        RoomAvailabilityRequest request = new RoomAvailabilityRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        Room room1 = new Room();
        room1.setId(1L);
        room1.setRoomNumber("101");
        room1.setType("Single");
        room1.setPricePerNight(new BigDecimal("100"));

        Room room2 = new Room();
        room2.setId(2L);
        room2.setRoomNumber("102");
        room2.setType("Double");
        room2.setPricePerNight(new BigDecimal("150"));

        List<Room> allRooms = Arrays.asList(room1, room2);

        when(roomRepository.findAll()).thenReturn(allRooms);
        when(bookingRepository.findOverlappingBookings(
                request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                request.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC)))
                .thenReturn(Collections.emptyList()); // No overlapping bookings

        List<RoomAvailabilityResponse> availableRooms = bookingService.getRoomsAvailable(request);

        assertNotNull(availableRooms);
        assertEquals(2, availableRooms.size());
        assertEquals("101", availableRooms.get(0).getRoomNumber());
        assertEquals("102", availableRooms.get(1).getRoomNumber());
    }

    @Test
    public void testGetRoomsAvailable_someBookingsOverlap() {
        LocalDate startDate = LocalDate.of(2024, 10, 20);
        LocalDate endDate = LocalDate.of(2024, 10, 25);
        RoomAvailabilityRequest request = new RoomAvailabilityRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        Room room1 = new Room();
        room1.setId(1L);
        room1.setRoomNumber("101");
        room1.setType("Single");
        room1.setPricePerNight(new BigDecimal("100"));

        Room room2 = new Room();
        room2.setId(2L);
        room2.setRoomNumber("102");
        room2.setType("Double");
        room2.setPricePerNight(new BigDecimal("150"));

        Room room3 = new Room();
        room3.setId(3L);
        room3.setRoomNumber("103");
        room3.setType("Suite");
        room3.setPricePerNight(new BigDecimal("200"));

        List<Room> allRooms = Arrays.asList(room1, room2, room3);

        // Room 101 is booked for part of the period
        Booking bookedRoom1Booking = new Booking();
        bookedRoom1Booking.setRoom(room1);
        bookedRoom1Booking.setCheckInDate(LocalDate.of(2024, 10, 18).atStartOfDay().toInstant(ZoneOffset.UTC));
        bookedRoom1Booking.setCheckOutDate(LocalDate.of(2024, 10, 22).atStartOfDay().toInstant(ZoneOffset.UTC));

        // Room 103 is booked for the entire period
        Booking bookedRoom3Booking = new Booking();
        bookedRoom3Booking.setRoom(room3);
        bookedRoom3Booking.setCheckInDate(LocalDate.of(2024, 10, 20).atStartOfDay().toInstant(ZoneOffset.UTC));
        bookedRoom3Booking.setCheckOutDate(LocalDate.of(2024, 10, 25).atStartOfDay().toInstant(ZoneOffset.UTC));

        List<Booking> overlappingBookings = Arrays.asList(bookedRoom1Booking, bookedRoom3Booking);


        when(roomRepository.findAll()).thenReturn(allRooms);
        when(bookingRepository.findOverlappingBookings(
                request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                request.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC)))
                .thenReturn(overlappingBookings);

        List<RoomAvailabilityResponse> availableRooms = bookingService.getRoomsAvailable(request);

        assertNotNull(availableRooms);
        assertEquals(1, availableRooms.size());
        assertEquals("102", availableRooms.get(0).getRoomNumber());
    }

    @Test
    public void testGetRoomsAvailable_roomAvailableFromCheckoutDate() {
        // Scenario: Room is booked from 22nd to 25th.
        // A user requests to book from 25th to 26th. The room should be available.
        LocalDate bookedCheckIn = LocalDate.of(2024, 12, 22);
        LocalDate bookedCheckOut = LocalDate.of(2024, 12, 25);

        Room room1 = new Room();
        room1.setId(1L);
        room1.setRoomNumber("101");
        room1.setType("Single");
        room1.setPricePerNight(new BigDecimal("100"));

        List<Room> allRooms = Collections.singletonList(room1);

        // Existing booking
        Booking existingBooking = new Booking();
        existingBooking.setRoom(room1);
        existingBooking.setCheckInDate(bookedCheckIn.atStartOfDay().toInstant(ZoneOffset.UTC));
        existingBooking.setCheckOutDate(bookedCheckOut.atStartOfDay().toInstant(ZoneOffset.UTC));

        // Request for a period starting on the checkout date
        LocalDate requestStartDate = LocalDate.of(2024, 12, 25);
        LocalDate requestEndDate = LocalDate.of(2024, 12, 26);
        RoomAvailabilityRequest request = new RoomAvailabilityRequest();
        request.setStartDate(requestStartDate);
        request.setEndDate(requestEndDate);

        // Mock that no bookings overlap with the requested period
        // because the overlap logic is checkIn < requestedCheckout AND checkOut > requestedCheckIn
        // For existingBooking (22, 25) and request (25, 26):
        // 22 < 26 (true) AND 25 > 25 (false) => false. So, no overlap.
        when(roomRepository.findAll()).thenReturn(allRooms);
        when(bookingRepository.findOverlappingBookings(
                request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                request.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC)))
                .thenReturn(Collections.emptyList());

        List<RoomAvailabilityResponse> availableRooms = bookingService.getRoomsAvailable(request);

        assertNotNull(availableRooms);
        assertEquals(1, availableRooms.size());
        assertEquals("101", availableRooms.get(0).getRoomNumber());
    }

    @Test
    public void testUpdateRoomStatusScheduled() {
        // Given
        Room room1 = new Room();
        room1.setId(1L);
        room1.setRoomNumber("101");
        room1.setStatus("BOOKED"); // Room is booked

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setRoom(room1);
        booking1.setCheckOutDate(Instant.now().minusSeconds(3600)); // Checkout date is in the past
        booking1.setStatus("CONFIRMED"); // Booking is confirmed

        List<Booking> expiredBookings = Arrays.asList(booking1);

        when(bookingRepository.findExpiredBookingsWithBookedRooms(any(Instant.class))).thenReturn(expiredBookings);
        when(roomRepository.save(any(Room.class))).thenReturn(room1); // Mock saving the room

        // When
        bookingService.updateRoomStatusScheduled();

        // Then
        assertEquals("AVAILABLE", room1.getStatus()); // Verify room status is updated to AVAILABLE
    }

    @Test
    public void testDeleteBooking_deletesGuestIfNoOtherBookings() {
        // Given
        Long bookingId = 1L;
        Long guestId = 100L;

        Guest guest = new Guest();
        guest.setId(guestId);
        guest.setFullName("Guest to be deleted");
        guest.setEmail("delete@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setStatus("BOOKED");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setCheckOutDate(LocalDate.now().plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(guestRepository.countBookingsByGuestId(guestId)).thenReturn(0L); // No other bookings for this guest
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        bookingService.deleteBooking(bookingId);

        // Then
        verify(bookingRepository, times(1)).delete(booking);
        verify(guestRepository, times(1)).delete(guest);
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    public void testDeleteBooking_doesNotDeleteGuestIfOtherBookingsExist() {
        // Given
        Long bookingId = 1L;
        Long guestId = 100L;

        Guest guest = new Guest();
        guest.setId(guestId);
        guest.setFullName("Guest with other bookings");
        guest.setEmail("keep@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setStatus("BOOKED");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setCheckOutDate(LocalDate.now().plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(guestRepository.countBookingsByGuestId(guestId)).thenReturn(1L); // Guest has 1 other booking
        when(roomRepository.save(any(Room.class))).thenReturn(room);


        // When
        bookingService.deleteBooking(bookingId);

        // Then
        verify(bookingRepository, times(1)).delete(booking);
        verify(guestRepository, times(0)).delete(guest); // Guest should NOT be deleted
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    public void testUpdateBooking_changesOldRoomStatusToAvailable() {
        // Given
        Long bookingId = 1L;
        
        Guest guest = new Guest();
        guest.setId(1L);
        guest.setFullName("Test Guest");

        Room oldRoom = new Room();
        oldRoom.setId(10L);
        oldRoom.setRoomNumber("101");
        oldRoom.setStatus("BOOKED"); // Old room is booked by this booking

        Room newRoom = new Room();
        newRoom.setId(20L);
        newRoom.setRoomNumber("202");
        newRoom.setStatus("AVAILABLE"); // New room is available initially

        Booking initialBooking = new Booking();
        initialBooking.setId(bookingId);
        initialBooking.setGuest(guest);
        initialBooking.setRoom(oldRoom);
        initialBooking.setCheckInDate(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
        initialBooking.setCheckOutDate(LocalDate.now().plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));
        initialBooking.setAmountPerNight(new BigDecimal("100"));
        initialBooking.setTotalAmount(new BigDecimal("200"));
        initialBooking.setStatus("CONFIRMED");
        initialBooking.setPayments(new java.util.ArrayList<>());

        BookingDTO updatedBookingDTO = new BookingDTO();
        updatedBookingDTO.setRoomNo("202"); // Request to change to new room
        updatedBookingDTO.setCheckInDate(LocalDate.now()); // Keep dates for total amount recalculation
        updatedBookingDTO.setCheckOutDate(LocalDate.now().plusDays(2));
        updatedBookingDTO.setNightlyRate(new BigDecimal("100"));

        // Mock behavior for finding the initial booking
        when(bookingRepository.findById(bookingId))
            .thenReturn(Optional.of(initialBooking));

        // Mock behavior for finding the new room
        when(roomRepository.findByRoomNumber("202")).thenReturn(Optional.of(newRoom));

        // Mock behavior for saving rooms:
        // First save should be for oldRoom becoming AVAILABLE
        // Second save should be for newRoom becoming BOOKED
        when(roomRepository.save(oldRoom)).thenReturn(oldRoom);
        when(roomRepository.save(newRoom)).thenReturn(newRoom);
        
        // Mock behavior for saving the updated booking
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock behavior for getBookingDetailsById which is called at the end of updateBooking
        Booking updatedBookingForGetDetails = new Booking();
        updatedBookingForGetDetails.setId(bookingId);
        updatedBookingForGetDetails.setGuest(guest);
        updatedBookingForGetDetails.setRoom(newRoom); // Ensure new room is set here
        updatedBookingForGetDetails.setCheckInDate(initialBooking.getCheckInDate());
        updatedBookingForGetDetails.setCheckOutDate(initialBooking.getCheckOutDate());
        updatedBookingForGetDetails.setAmountPerNight(initialBooking.getAmountPerNight());
        updatedBookingForGetDetails.setTotalAmount(initialBooking.getTotalAmount());
        updatedBookingForGetDetails.setStatus("CONFIRMED");
        updatedBookingForGetDetails.setPayments(new java.util.ArrayList<>());


        when(bookingRepository.findById(bookingId))
            .thenReturn(Optional.of(initialBooking)) // First call from updateBooking method
            .thenReturn(Optional.of(updatedBookingForGetDetails)); // Second call from getBookingDetailsById method


        // When
        bookingService.updateBooking(bookingId, updatedBookingDTO);

        // Then
        assertEquals("AVAILABLE", oldRoom.getStatus()); // Old room should now be AVAILABLE
        assertEquals("BOOKED", newRoom.getStatus());   // New room should now be BOOKED

        // Verify save calls
        verify(roomRepository, times(1)).save(oldRoom); // Old room saved once
        verify(roomRepository, times(1)).save(newRoom); // New room saved once
        verify(bookingRepository, times(2)).findById(bookingId); // Once for updateBooking, once for getBookingDetailsById
        verify(bookingRepository, times(1)).save(any(Booking.class)); // Booking saved once
    }
}