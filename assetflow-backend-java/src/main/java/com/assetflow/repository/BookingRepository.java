package com.assetflow.repository;

import com.assetflow.model.Booking;
import com.assetflow.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookedById(Long bookedById);

    List<Booking> findByAssetIdAndStatus(Long assetId, BookingStatus status);

    List<Booking> findByAssetId(Long assetId);

    List<Booking> findByStatus(BookingStatus status);

    /**
     * Finds bookings for the given asset that overlap the requested time window
     * and are not already cancelled/completed. Used for overlap validation:
     * two ranges [s1,e1) and [s2,e2) overlap when s1 < e2 AND s2 < e1.
     */
    @Query("SELECT b FROM Booking b WHERE b.asset.id = :assetId " +
            "AND b.status IN ('UPCOMING', 'ONGOING') " +
            "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(@Param("assetId") Long assetId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Booking b WHERE b.asset.id = :assetId " +
            "AND b.id <> :excludeBookingId " +
            "AND b.status IN ('UPCOMING', 'ONGOING') " +
            "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlappingBookingsExcluding(@Param("assetId") Long assetId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime,
                                                     @Param("excludeBookingId") Long excludeBookingId);
}
