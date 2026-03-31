package com.musicalnote.musicalnote.service;

import com.musicalnote.musicalnote.dto.ReviewDto;
import com.musicalnote.musicalnote.entity.Instrument;
import com.musicalnote.musicalnote.entity.Rental;
import com.musicalnote.musicalnote.entity.Review;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import com.musicalnote.musicalnote.enums.RentalStatus;
import com.musicalnote.musicalnote.enums.Role;
import com.musicalnote.musicalnote.exception.BusinessException;
import com.musicalnote.musicalnote.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RentalService rentalService;
    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private ReviewService reviewService;

    private User student;
    private Instrument guitar;
    private Rental returnedRental;
    private ReviewDto.Request reviewRequest;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L).name("Jane Doe").email("jane@email.com")
                .role(Role.STUDENT).build();

        guitar = Instrument.builder()
                .id(1L).name("Yamaha Guitar").category(Category.STRING)
                .brand("Yamaha").condition(Condition.NEW)
                .available(true).pricePerDay(15.0).build();

        returnedRental = Rental.builder()
                .id(1L).user(student).instrument(guitar)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().minusDays(1))
                .status(RentalStatus.RETURNED)
                .totalPrice(90.0)
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();

        reviewRequest = new ReviewDto.Request();
        reviewRequest.setRentalId(1L);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Excellent guitar!");
    }

    @Test
    void create_success() {
        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(rentalService.findById(1L)).thenReturn(returnedRental);
        when(reviewRepository.existsByRentalId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r = Review.builder()
                    .id(1L).user(student).instrument(guitar)
                    .rental(returnedRental).rating(5)
                    .comment("Excellent guitar!")
                    .createdAt(LocalDateTime.now()).build();
            return r;
        });

        ReviewDto.Response response = reviewService.create(1L, reviewRequest, student);

        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent guitar!");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void create_throwsException_whenRentalNotReturned() {
        returnedRental.setStatus(RentalStatus.ACTIVE);
        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(rentalService.findById(1L)).thenReturn(returnedRental);

        assertThatThrownBy(() -> reviewService.create(1L, reviewRequest, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RETURNED");
    }

    @Test
    void create_throwsException_whenAlreadyReviewed() {
        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(rentalService.findById(1L)).thenReturn(returnedRental);
        when(reviewRepository.existsByRentalId(1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(1L, reviewRequest, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already reviewed");
    }

    @Test
    void create_throwsException_whenNotRentalOwner() {
        User otherUser = User.builder().id(99L).role(Role.STUDENT).build();
        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(rentalService.findById(1L)).thenReturn(returnedRental);

        assertThatThrownBy(() -> reviewService.create(1L, reviewRequest, otherUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("own rentals");
    }

    @Test
    void create_throwsException_whenRentalInstrumentMismatch() {
        Instrument piano = Instrument.builder().id(99L).build();
        returnedRental = Rental.builder()
                .id(1L).user(student).instrument(piano)
                .status(RentalStatus.RETURNED).totalPrice(50.0)
                .createdAt(LocalDateTime.now()).build();

        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(rentalService.findById(1L)).thenReturn(returnedRental);

        assertThatThrownBy(() -> reviewService.create(1L, reviewRequest, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void getByInstrument_returnsReviews() {
        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(reviewRepository.findByInstrumentId(1L)).thenReturn(List.of());

        List<ReviewDto.Response> result = reviewService.getByInstrument(1L);

        assertThat(result).isEmpty();
        verify(reviewRepository).findByInstrumentId(1L);
    }
}
