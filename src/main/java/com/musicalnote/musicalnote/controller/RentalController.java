package com.musicalnote.musicalnote.controller;

import com.musicalnote.musicalnote.dto.RentalDto;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    public ResponseEntity<RentalDto.Response> book(
            @Valid @RequestBody RentalDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(201).body(rentalService.book(request, currentUser));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RentalDto.Response>> getMyRentals(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(rentalService.getMyRentals(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalDto.Response> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(rentalService.getById(id, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<RentalDto.Response>> getAllRentals() {
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<RentalDto.Response> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(rentalService.cancel(id, currentUser));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<RentalDto.Response> markReturned(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.markReturned(id));
    }
}
