package com.musicalnote.musicalnote.service;

import com.musicalnote.musicalnote.dto.RentalDto;
import com.musicalnote.musicalnote.entity.Instrument;
import com.musicalnote.musicalnote.entity.Rental;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.enums.RentalStatus;
import com.musicalnote.musicalnote.exception.BusinessException;
import com.musicalnote.musicalnote.exception.ResourceNotFoundException;
import com.musicalnote.musicalnote.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final InstrumentService instrumentService;

    @Transactional
    public RentalDto.Response book(RentalDto.Request request, User currentUser) {
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new BusinessException("Start date must be before end date");
        }

        Instrument instrument = instrumentService.findById(request.getInstrumentId());

        if (!instrument.isAvailable()) {
            throw new BusinessException("Instrument is not available for rental");
        }

        boolean hasOverlap = rentalRepository.existsOverlappingRental(
                instrument.getId(), request.getStartDate(), request.getEndDate());

        if (hasOverlap) {
            throw new BusinessException("Instrument is already booked for the selected dates");
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        double totalPrice = days * instrument.getPricePerDay();

        Rental rental = Rental.builder()
                .user(currentUser)
                .instrument(instrument)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(RentalStatus.PENDING)
                .totalPrice(totalPrice)
                .build();

        return toResponse(rentalRepository.save(rental));
    }

    public List<RentalDto.Response> getMyRentals(User currentUser) {
        return rentalRepository.findByUserId(currentUser.getId())
                .stream().map(this::toResponse).toList();
    }

    public RentalDto.Response getById(Long id, User currentUser) {
        Rental rental = findById(id);
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied");
        }
        return toResponse(rental);
    }

    public List<RentalDto.Response> getAllRentals() {
        return rentalRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RentalDto.Response cancel(Long id, User currentUser) {
        Rental rental = findById(id);

        if (!rental.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only cancel your own rentals");
        }
        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new BusinessException("Only PENDING rentals can be cancelled");
        }
        if (LocalDate.now().isAfter(rental.getStartDate().minusDays(1))) {
            throw new BusinessException("Cannot cancel less than 24 hours before the start date");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        return toResponse(rentalRepository.save(rental));
    }

    @Transactional
    public RentalDto.Response markReturned(Long id) {
        Rental rental = findById(id);

        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.PENDING) {
            throw new BusinessException("Rental cannot be marked as returned in its current status");
        }

        rental.setStatus(RentalStatus.RETURNED);
        rental.getInstrument().setAvailable(true);
        return toResponse(rentalRepository.save(rental));
    }

    public Rental findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + id));
    }

    private RentalDto.Response toResponse(Rental rental) {
        return RentalDto.Response.builder()
                .id(rental.getId())
                .userId(rental.getUser().getId())
                .userName(rental.getUser().getName())
                .instrument(instrumentService.toResponse(rental.getInstrument()))
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .status(rental.getStatus())
                .totalPrice(rental.getTotalPrice())
                .createdAt(rental.getCreatedAt())
                .build();
    }
}
