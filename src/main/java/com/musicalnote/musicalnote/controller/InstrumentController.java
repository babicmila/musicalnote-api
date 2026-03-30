package com.musicalnote.musicalnote.controller;

import com.musicalnote.musicalnote.dto.InstrumentDto;
import com.musicalnote.musicalnote.dto.ReviewDto;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import com.musicalnote.musicalnote.service.InstrumentService;
import com.musicalnote.musicalnote.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<InstrumentDto.Response>> getAll(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Condition condition,
            @RequestParam(required = false) Boolean available) {
        return ResponseEntity.ok(instrumentService.getAll(category, condition, available));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstrumentDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(instrumentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<InstrumentDto.Response> create(@Valid @RequestBody InstrumentDto.Request request) {
        return ResponseEntity.status(201).body(instrumentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstrumentDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody InstrumentDto.Request request) {
        return ResponseEntity.ok(instrumentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instrumentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDto.Response>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getByInstrument(id));
    }
}
