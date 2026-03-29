package com.musicalnote.musicalnote.service;

import com.musicalnote.musicalnote.dto.InstrumentDto;
import com.musicalnote.musicalnote.entity.Instrument;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import com.musicalnote.musicalnote.exception.ResourceNotFoundException;
import com.musicalnote.musicalnote.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public List<InstrumentDto.Response> getAll(Category category, Condition condition, Boolean available) {
        List<Instrument> instruments;

        if (available != null && available && category != null && condition != null) {
            instruments = instrumentRepository.findByAvailableTrueAndCategoryAndCondition(category, condition);
        } else if (available != null && available && category != null) {
            instruments = instrumentRepository.findByAvailableTrueAndCategory(category);
        } else if (available != null && available && condition != null) {
            instruments = instrumentRepository.findByAvailableTrueAndCondition(condition);
        } else if (available != null && available) {
            instruments = instrumentRepository.findByAvailableTrue();
        } else if (category != null) {
            instruments = instrumentRepository.findByCategory(category);
        } else if (condition != null) {
            instruments = instrumentRepository.findByCondition(condition);
        } else {
            instruments = instrumentRepository.findAll();
        }

        return instruments.stream().map(this::toResponse).toList();
    }

    public InstrumentDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public InstrumentDto.Response create(InstrumentDto.Request request) {
        Instrument instrument = Instrument.builder()
                .name(request.getName())
                .category(request.getCategory())
                .brand(request.getBrand())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .condition(request.getCondition())
                .available(request.getAvailable())
                .pricePerDay(request.getPricePerDay())
                .build();

        return toResponse(instrumentRepository.save(instrument));
    }

    public InstrumentDto.Response update(Long id, InstrumentDto.Request request) {
        Instrument instrument = findById(id);
        instrument.setName(request.getName());
        instrument.setCategory(request.getCategory());
        instrument.setBrand(request.getBrand());
        instrument.setDescription(request.getDescription());
        instrument.setImageUrl(request.getImageUrl());
        instrument.setCondition(request.getCondition());
        instrument.setAvailable(request.getAvailable());
        instrument.setPricePerDay(request.getPricePerDay());

        return toResponse(instrumentRepository.save(instrument));
    }

    public void delete(Long id) {
        findById(id);
        instrumentRepository.deleteById(id);
    }

    public Instrument findById(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrument not found with id: " + id));
    }

    public InstrumentDto.Response toResponse(Instrument instrument) {
        return InstrumentDto.Response.builder()
                .id(instrument.getId())
                .name(instrument.getName())
                .category(instrument.getCategory())
                .brand(instrument.getBrand())
                .description(instrument.getDescription())
                .imageUrl(instrument.getImageUrl())
                .condition(instrument.getCondition())
                .available(instrument.isAvailable())
                .pricePerDay(instrument.getPricePerDay())
                .build();
    }
}
