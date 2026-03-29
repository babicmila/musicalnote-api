package com.musicalnote.musicalnote.service;

import com.musicalnote.musicalnote.dto.ReviewDto;
import com.musicalnote.musicalnote.entity.Instrument;
import com.musicalnote.musicalnote.entity.Rental;
import com.musicalnote.musicalnote.entity.Review;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.enums.RentalStatus;
import com.musicalnote.musicalnote.exception.BusinessException;
import com.musicalnote.musicalnote.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentalService rentalService;
    private final InstrumentService instrumentService;

    public ReviewDto.Response create(Long instrumentId, ReviewDto.Request request, User currentUser) {
        Instrument instrument = instrumentService.findById(instrumentId);
        Rental rental = rentalService.findById(request.getRentalId());

        if (!rental.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only review your own rentals");
        }
        if (rental.getStatus() != RentalStatus.RETURNED) {
            throw new BusinessException("You can only review a completed (RETURNED) rental");
        }
        if (!rental.getInstrument().getId().equals(instrumentId)) {
            throw new BusinessException("Rental does not match the given instrument");
        }
        if (reviewRepository.existsByRentalId(rental.getId())) {
            throw new BusinessException("You have already reviewed this rental");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .user(currentUser)
                .instrument(instrument)
                .rental(rental)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    public List<ReviewDto.Response> getByInstrument(Long instrumentId) {
        instrumentService.findById(instrumentId);
        return reviewRepository.findByInstrumentId(instrumentId)
                .stream().map(this::toResponse).toList();
    }

    private ReviewDto.Response toResponse(Review review) {
        return ReviewDto.Response.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .instrumentId(review.getInstrument().getId())
                .rentalId(review.getRental().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
