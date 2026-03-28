package com.musicalnote.musicalnote.repository;

import com.musicalnote.musicalnote.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByInstrumentId(Long instrumentId);
    boolean existsByRentalId(Long rentalId);
}
