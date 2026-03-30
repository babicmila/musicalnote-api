package com.musicalnote.musicalnote.controller;

import com.musicalnote.musicalnote.dto.ReviewDto;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instruments/{instrumentId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto.Response> create(
            @PathVariable Long instrumentId,
            @Valid @RequestBody ReviewDto.Request request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(201).body(reviewService.create(instrumentId, request, currentUser));
    }
}
