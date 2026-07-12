package com.assetflow.service;

import com.assetflow.dto.BookingDTO;
import com.assetflow.dto.RescheduleDTO;
import com.assetflow.exception.BookingConflictException;
import com.assetflow.exception.InvalidBookingException;
import com.assetflow.exception.InvalidStatusTransitionException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.model.*;
import com.assetflow.repository.AssetRepository;
import com.assetflow.repository.BookingRepository;
import com.assetflow.repository.DepartmentRepository;
import com.assetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Creates a time-slot booking for a shared/bookable resource.
     * Overlap rule: two ranges [s1,e1) and [s2,e2) overlap when s1 < e2 AND s2 < e1,
     * so a booking that starts exactly when another ends (10:00-11:00 after 9:00-10:00) is allowed.
     */
    public Booking createBooking(BookingDTO dto) {
        validateTimeRange(dto.getStartTime(), dto.getEndTime());

        Asset asset = getAssetOrThrow(dto.getAssetId());
        if (!asset.isSharedBookable()) {
            throw new InvalidBookingException("Asset " + asset.getAssetTag() + " is not flagged as shared/bookable");
        }
        if (asset.getStatus() == AssetStatus.UNDER_MAINTENANCE || asset.getStatus() == AssetStatus.LOST
                || asset.getStatus() == AssetStatus.RETIRED || asset.getStatus() == AssetStatus.DISPOSED) {
            throw new InvalidBookingException(
                    "Asset " + asset.getAssetTag() + " is not currently bookable (status: " + asset.getStatus() + ")");
        }

        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                asset.getId(), dto.getStartTime(), dto.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new BookingConflictException(
                    "Asset " + asset.getAssetTag() + " is already booked for an overlapping time slot");
        }

        User bookedBy = getUserOrThrow(dto.getBookedById());
        Department department = dto.getDepartmentId() != null ? getDepartmentOrThrow(dto.getDepartmentId()) : null;

        Booking booking = Booking.builder()
                .asset(asset)
                .bookedBy(bookedBy)
                .department(department)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .purpose(dto.getPurpose())
                .status(resolveInitialStatus(dto.getStartTime(), dto.getEndTime()))
                .build();

        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot cancel a booking that is already " + booking.getStatus());
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public Booking rescheduleBooking(Long bookingId, RescheduleDTO dto) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot reschedule a booking that is already " + booking.getStatus());
        }

        validateTimeRange(dto.getNewStartTime(), dto.getNewEndTime());

        List<Booking> overlaps = bookingRepository.findOverlappingBookingsExcluding(
                booking.getAsset().getId(), dto.getNewStartTime(), dto.getNewEndTime(), booking.getId());
        if (!overlaps.isEmpty()) {
            throw new BookingConflictException(
                    "Asset " + booking.getAsset().getAssetTag() + " is already booked for an overlapping time slot");
        }

        booking.setStartTime(dto.getNewStartTime());
        booking.setEndTime(dto.getNewEndTime());
        booking.setStatus(resolveInitialStatus(dto.getNewStartTime(), dto.getNewEndTime()));

        return bookingRepository.save(booking);
    }

    /** Returns existing bookings for an asset within [start, end), used to render a calendar / availability view. */
    @Transactional(readOnly = true)
    public List<Booking> getAvailability(Long assetId, LocalDateTime start, LocalDateTime end) {
        getAssetOrThrow(assetId);
        if (start != null && end != null) {
            validateTimeRange(start, end);
            return bookingRepository.findOverlappingBookings(assetId, start, end);
        }
        return bookingRepository.findByAssetId(assetId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByBookedById(userId);
    }

    /**
     * Re-evaluates and persists booking statuses based on the current time. Intended to be called
     * periodically (e.g. by a scheduled job) so UPCOMING bookings flip to ONGOING and then COMPLETED.
     */
    public void refreshBookingStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> active = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.UPCOMING || b.getStatus() == BookingStatus.ONGOING)
                .toList();

        for (Booking booking : active) {
            BookingStatus resolved = resolveInitialStatus(booking.getStartTime(), booking.getEndTime());
            if (now.isAfter(booking.getEndTime())) {
                resolved = BookingStatus.COMPLETED;
            }
            if (resolved != booking.getStatus()) {
                booking.setStatus(resolved);
                bookingRepository.save(booking);
            }
        }
    }

    private BookingStatus resolveInitialStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return BookingStatus.UPCOMING;
        } else if (now.isAfter(end)) {
            return BookingStatus.COMPLETED;
        }
        return BookingStatus.ONGOING;
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new InvalidBookingException("Both startTime and endTime are required");
        }
        if (!end.isAfter(start)) {
            throw new InvalidBookingException("endTime must be after startTime");
        }
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", id));
    }

    private Asset getAssetOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Asset", id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    private Department getDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }
}
