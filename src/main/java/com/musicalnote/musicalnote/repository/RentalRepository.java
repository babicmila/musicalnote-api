package com.musicalnote.musicalnote.repository;

import com.musicalnote.musicalnote.entity.Rental;
import com.musicalnote.musicalnote.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserId(Long userId);

    List<Rental> findByStatus(RentalStatus status);

    // Check for overlapping rentals on an instrument (for conflict detection)
    @Query("""
        SELECT COUNT(r) > 0 FROM Rental r
        WHERE r.instrument.id = :instrumentId
        AND r.status IN ('PENDING', 'ACTIVE')
        AND r.startDate <= :endDate
        AND r.endDate >= :startDate
    """)
    boolean existsOverlappingRental(
            @Param("instrumentId") Long instrumentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsByRentalAndUserIdAndStatus(Long rentalId, Long userId, RentalStatus status);
}
