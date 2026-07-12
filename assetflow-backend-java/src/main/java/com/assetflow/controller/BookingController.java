package com.assetflow.controller;

import com.assetflow.dto.BookingDTO;
import com.assetflow.dto.RescheduleDTO;
import com.assetflow.model.Booking;
import com.assetflow.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    // Create Booking
    @PostMapping
    public Booking createBooking(@RequestBody BookingDTO dto) {
        return bookingService.createBooking(dto);
    }

    // Cancel Booking
    @PatchMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    // Reschedule Booking
    @PatchMapping("/{id}/reschedule")
    public Booking rescheduleBooking(
            @PathVariable Long id,
            @RequestBody RescheduleDTO dto) {
        return bookingService.rescheduleBooking(id, dto);
    }

    // Asset Availability
    @GetMapping("/availability/{assetId}")
    public List<Booking> getAvailability(
            @PathVariable Long assetId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        return bookingService.getAvailability(assetId, start, end);
    }

    // User Bookings
    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsForUser(@PathVariable Long userId) {
        return bookingService.getBookingsForUser(userId);
    }

    // Refresh Statuses
    @PostMapping("/refresh")
    public String refreshBookingStatuses() {
        bookingService.refreshBookingStatuses();
        return "Booking statuses refreshed successfully.";
    }
}