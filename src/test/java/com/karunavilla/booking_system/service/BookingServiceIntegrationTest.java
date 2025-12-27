package com.karunavilla.booking_system.service;

import com.karunavilla.booking_system.Entity.Booking;
import com.karunavilla.booking_system.Entity.Guest;
import com.karunavilla.booking_system.Entity.Room;
import com.karunavilla.booking_system.model.RoomAvailabilityRequest;
import com.karunavilla.booking_system.model.RoomAvailabilityResponse;
import com.karunavilla.booking_system.repository.BookingRepository;
import com.karunavilla.booking_system.repository.GuestRepository;
import com.karunavilla.booking_system.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test") // Although checking src/test/resources/application.yml is automatic usually
public class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Room room101;
    private Room room102;

    @BeforeEach
    public void setup() {
        bookingRepository.deleteAll();
        guestRepository.deleteAll();
        roomRepository.deleteAll();

        room101 = new Room();
        room101.setRoomNumber("101");
        room101.setType("Deluxe");
        room101.setPricePerNight(BigDecimal.valueOf(100));
        room101.setStatus("AVAILABLE");
        room101.setRoomName("Room 101");
        roomRepository.save(room101);

        room102 = new Room();
        room102.setRoomNumber("102");
        room102.setType("Standard");
        room102.setPricePerNight(BigDecimal.valueOf(80));
        room102.setStatus("AVAILABLE");
        room102.setRoomName("Room 102");
        roomRepository.save(room102);
    }

    @Test
    @Transactional
    public void testAvailabilityBoundary() {
        // Booking 1: 30 Dec to 31 Dec on Room 101
        createBooking(room101, LocalDate.of(2025, 12, 30), LocalDate.of(2025, 12, 31));

        // Check Availability: 31 Dec to 1 Jan
        RoomAvailabilityRequest request = new RoomAvailabilityRequest();
        request.setStartDate(LocalDate.of(2025, 12, 31));
        request.setEndDate(LocalDate.of(2026, 1, 1));

        List<RoomAvailabilityResponse> availableRooms = bookingService.getRoomsAvailable(request);

        // Expectation: Room 101 should be available because checkout is 31st Dec 00:00 and checkin is 31st Dec 00:00.
        // Room 102 is also available.
        // Total 2.
        
        System.out.println("Available rooms: " + availableRooms.size());
        availableRooms.forEach(r -> System.out.println("Room: " + r.getRoomNumber()));

        assertTrue(availableRooms.stream().anyMatch(r -> r.getRoomNumber().equals("101")), "Room 101 should be available");
        assertEquals(2, availableRooms.size());
    }

    @Test
    @Transactional
    public void testUserScenario() {
        // Replicating the user's implicit state:
        // 28-29: 1 room available (Implies 1 booking) -> Book 101 for 28-29
        createBooking(room101, LocalDate.of(2025, 12, 28), LocalDate.of(2025, 12, 29));
        
        // 29-30: 2 rooms available -> No bookings
        
        // 30-31: 1 room available -> Book 101 for 30-31
        createBooking(room101, LocalDate.of(2025, 12, 30), LocalDate.of(2025, 12, 31));

        // 31-1: 0 rooms available -> Book 101 AND 102 for 31-1
        createBooking(room101, LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1));
        createBooking(room102, LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1));

        // Now verify queries
        
        // 28-29
        RoomAvailabilityRequest req1 = new RoomAvailabilityRequest();
        req1.setStartDate(LocalDate.of(2025, 12, 28));
        req1.setEndDate(LocalDate.of(2025, 12, 29));
        assertEquals(1, bookingService.getRoomsAvailable(req1).size(), "28-29 should have 1 room");

        // 29-30
        RoomAvailabilityRequest req2 = new RoomAvailabilityRequest();
        req2.setStartDate(LocalDate.of(2025, 12, 29));
        req2.setEndDate(LocalDate.of(2025, 12, 30));
        assertEquals(2, bookingService.getRoomsAvailable(req2).size(), "29-30 should have 2 rooms");

        // 30-31
        RoomAvailabilityRequest req3 = new RoomAvailabilityRequest();
        req3.setStartDate(LocalDate.of(2025, 12, 30));
        req3.setEndDate(LocalDate.of(2025, 12, 31));
        assertEquals(1, bookingService.getRoomsAvailable(req3).size(), "30-31 should have 1 room");

        // 31-1
        RoomAvailabilityRequest req4 = new RoomAvailabilityRequest();
        req4.setStartDate(LocalDate.of(2025, 12, 31));
        req4.setEndDate(LocalDate.of(2026, 1, 1));
        assertEquals(0, bookingService.getRoomsAvailable(req4).size(), "31-1 should have 0 rooms");
    }

    @Test
    @Transactional
    public void testCancelledBookingShouldNotBlock() {
        // Create a booking that is CANCELLED (or just not CONFIRMED if we assume only CONFIRMED blocks)
        // Since the current code ignores status, any status will block.
        // We want to prove that it BLOCKS now, and after fix it won't.
        
        Guest guest = new Guest();
        guest.setFullName("Cancelled Guest");
        guest.setMobileNumber("0000000000");
        guest = guestRepository.save(guest);

        Booking booking = new Booking();
        booking.setRoom(room101);
        booking.setGuest(guest);
        booking.setCheckInDate(LocalDate.of(2025, 12, 31).atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setCheckOutDate(LocalDate.of(2026, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setStatus("CANCELLED"); // Status other than CONFIRMED
        booking.setTotalAmount(BigDecimal.TEN);
        bookingRepository.save(booking);

        // Check Availability: 31 Dec to 1 Jan
        RoomAvailabilityRequest request = new RoomAvailabilityRequest();
        request.setStartDate(LocalDate.of(2025, 12, 31));
        request.setEndDate(LocalDate.of(2026, 1, 1));

        List<RoomAvailabilityResponse> availableRooms = bookingService.getRoomsAvailable(request);
        
        // CURRENT BEHAVIOR (Before Fix): CANCELLED booking is counted as overlapping because query ignores status.
        // So Room 101 should NOT be available.
        // We assert what we expect AFTER the fix, or we assert the current bug?
        // Let's assert the behavior we WANT (the test should fail now).
        
        assertTrue(availableRooms.stream().anyMatch(r -> r.getRoomNumber().equals("101")), 
            "Room 101 should be available because the booking is CANCELLED");
    }

    private void createBooking(Room room, LocalDate checkIn, LocalDate checkOut) {
        Guest guest = new Guest();
        guest.setFullName("Test Guest");
        guest.setMobileNumber("1234567890");
        guest = guestRepository.save(guest);

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setCheckInDate(checkIn.atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setCheckOutDate(checkOut.atStartOfDay().toInstant(ZoneOffset.UTC));
        booking.setStatus("CONFIRMED");
        // Required fields
        booking.setTotalAmount(BigDecimal.TEN);
        bookingRepository.save(booking);
    }
}
