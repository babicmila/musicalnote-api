package com.musicalnote.musicalnote.service;

import com.musicalnote.musicalnote.dto.RentalDto;
import com.musicalnote.musicalnote.entity.Instrument;
import com.musicalnote.musicalnote.entity.Rental;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import com.musicalnote.musicalnote.enums.RentalStatus;
import com.musicalnote.musicalnote.enums.Role;
import com.musicalnote.musicalnote.exception.BusinessException;
import com.musicalnote.musicalnote.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private RentalService rentalService;

    private User student;
    private Instrument guitar;
    private Rental rental;
    private RentalDto.Request rentalRequest;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L).name("Jane Doe").email("jane@email.com")
                .role(Role.STUDENT).build();

        guitar = Instrument.builder()
                .id(1L).name("Yamaha Guitar").category(Category.STRING)
                .brand("Yamaha").condition(Condition.NEW)
                .available(true).pricePerDay(15.0).build();

        rental = Rental.builder()
                .id(1L).user(student).instrument(guitar)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(7))
                .status(RentalStatus.PENDING)
                .totalPrice(90.0)
                .createdAt(LocalDateTime.now())
                .build();

        rentalRequest = new RentalDto.Request();
        rentalRequest.setInstrumentId(1L);
        rentalRequest.setStartDate(LocalDate.now().plusDays(1));
        rentalRequest.setEndDate(LocalDate.now().plusDays(7));
    }

    @Test
    void book_success() {
        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(rentalRepository.existsOverlappingRental(any(), any(), any())).thenReturn(false);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(instrumentService.toResponse(guitar)).thenCallRealMethod();

        RentalDto.Response response = rentalService.book(rentalRequest, student);

        assertThat(response.getStatus()).isEqualTo(RentalStatus.PENDING);
        assertThat(response.getTotalPrice()).isEqualTo(90.0);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void book_throwsException_whenInstrumentUnavailable() {
        guitar.setAvailable(false);
        when(instrumentService.findById(1L)).thenReturn(guitar);

        assertThatThrownBy(() -> rentalService.book(rentalRequest, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void book_throwsException_whenDatesOverlap() {
        when(instrumentService.findById(1L)).thenReturn(guitar);
        when(rentalRepository.existsOverlappingRental(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> rentalService.book(rentalRequest, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void book_throwsException_whenStartDateAfterEndDate() {
        rentalRequest.setStartDate(LocalDate.now().plusDays(7));
        rentalRequest.setEndDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> rentalService.book(rentalRequest, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Start date must be before end date");
    }

    @Test
    void cancel_success_whenPendingAndOwner() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any())).thenReturn(rental);

        RentalDto.Response response = rentalService.cancel(1L, student);

        verify(rentalRepository).save(rental);
    }

    @Test
    void cancel_throwsException_whenNotOwner() {
        User otherUser = User.builder().id(99L).role(Role.STUDENT).build();
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        assertThatThrownBy(() -> rentalService.cancel(1L, otherUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("own rentals");
    }

    @Test
    void cancel_throwsException_whenNotPending() {
        rental.setStatus(RentalStatus.ACTIVE);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        assertThatThrownBy(() -> rentalService.cancel(1L, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void markReturned_success() {
        rental.setStatus(RentalStatus.ACTIVE);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any())).thenReturn(rental);

        rentalService.markReturned(1L);

        assertThat(rental.getStatus()).isEqualTo(RentalStatus.RETURNED);
        assertThat(guitar.isAvailable()).isTrue();
    }

    @Test
    void getMyRentals_returnsOnlyUserRentals() {
        when(rentalRepository.findByUserId(1L)).thenReturn(List.of(rental));

        List<RentalDto.Response> result = rentalService.getMyRentals(student);

        assertThat(result).hasSize(1);
        verify(rentalRepository).findByUserId(1L);
    }
}
