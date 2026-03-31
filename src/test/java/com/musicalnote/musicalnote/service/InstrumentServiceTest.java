package com.musicalnote.musicalnote.service;

import com.musicalnote.musicalnote.dto.InstrumentDto;
import com.musicalnote.musicalnote.entity.Instrument;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import com.musicalnote.musicalnote.exception.ResourceNotFoundException;
import com.musicalnote.musicalnote.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private InstrumentService instrumentService;

    private Instrument guitar;
    private InstrumentDto.Request guitarRequest;

    @BeforeEach
    void setUp() {
        guitar = Instrument.builder()
                .id(1L)
                .name("Yamaha Guitar")
                .category(Category.STRING)
                .brand("Yamaha")
                .condition(Condition.NEW)
                .available(true)
                .pricePerDay(15.0)
                .build();

        guitarRequest = new InstrumentDto.Request();
        guitarRequest.setName("Yamaha Guitar");
        guitarRequest.setCategory(Category.STRING);
        guitarRequest.setBrand("Yamaha");
        guitarRequest.setCondition(Condition.NEW);
        guitarRequest.setAvailable(true);
        guitarRequest.setPricePerDay(15.0);
    }

    @Test
    void getAll_returnsAllInstruments_whenNoFilters() {
        when(instrumentRepository.findAll()).thenReturn(List.of(guitar));

        List<InstrumentDto.Response> result = instrumentService.getAll(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Yamaha Guitar");
    }

    @Test
    void getAll_returnsAvailableOnly_whenAvailableFilterTrue() {
        when(instrumentRepository.findByAvailableTrue()).thenReturn(List.of(guitar));

        List<InstrumentDto.Response> result = instrumentService.getAll(null, null, true);

        assertThat(result).hasSize(1);
        verify(instrumentRepository).findByAvailableTrue();
    }

    @Test
    void getById_returnsInstrument_whenExists() {
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));

        InstrumentDto.Response result = instrumentService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBrand()).isEqualTo("Yamaha");
    }

    @Test
    void getById_throwsException_whenNotFound() {
        when(instrumentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instrumentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsInstrument() {
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(guitar);

        InstrumentDto.Response result = instrumentService.create(guitarRequest);

        assertThat(result.getName()).isEqualTo("Yamaha Guitar");
        assertThat(result.getPricePerDay()).isEqualTo(15.0);
        verify(instrumentRepository).save(any(Instrument.class));
    }

    @Test
    void update_updatesAndReturnsInstrument() {
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(guitar);

        guitarRequest.setName("Updated Guitar");
        InstrumentDto.Response result = instrumentService.update(1L, guitarRequest);

        verify(instrumentRepository).save(guitar);
    }

    @Test
    void delete_deletesInstrument_whenExists() {
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));

        instrumentService.delete(1L);

        verify(instrumentRepository).deleteById(1L);
    }

    @Test
    void delete_throwsException_whenNotFound() {
        when(instrumentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instrumentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(instrumentRepository, never()).deleteById(any());
    }
}
