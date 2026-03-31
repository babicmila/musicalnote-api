package com.musicalnote.musicalnote.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicalnote.musicalnote.dto.InstrumentDto;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import com.musicalnote.musicalnote.exception.ResourceNotFoundException;
import com.musicalnote.musicalnote.security.JwtFilter;
import com.musicalnote.musicalnote.service.InstrumentService;
import com.musicalnote.musicalnote.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InstrumentController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
class InstrumentControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @MockBean
        private InstrumentService instrumentService;
        @MockBean
        private ReviewService reviewService;

        private InstrumentDto.Response guitarResponse;
        private InstrumentDto.Request guitarRequest;

        @BeforeEach
        void setUp() {
                guitarResponse = InstrumentDto.Response.builder()
                                .id(1L).name("Yamaha Guitar").category(Category.STRING)
                                .brand("Yamaha").condition(Condition.NEW)
                                .available(true).pricePerDay(15.0).build();

                guitarRequest = new InstrumentDto.Request();
                guitarRequest.setName("Yamaha Guitar");
                guitarRequest.setCategory(Category.STRING);
                guitarRequest.setBrand("Yamaha");
                guitarRequest.setCondition(Condition.NEW);
                guitarRequest.setAvailable(true);
                guitarRequest.setPricePerDay(15.0);
        }

        @Test
        void getAll_returnsInstrumentList() throws Exception {
                when(instrumentService.getAll(null, null, null)).thenReturn(List.of(guitarResponse));

                mockMvc.perform(get("/api/instruments"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Yamaha Guitar"))
                                .andExpect(jsonPath("$[0].category").value("STRING"));
        }

        @Test
        void getById_returnsInstrument_whenExists() throws Exception {
                when(instrumentService.getById(1L)).thenReturn(guitarResponse);

                mockMvc.perform(get("/api/instruments/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.brand").value("Yamaha"));
        }

        @Test
        void getById_returns404_whenNotFound() throws Exception {
                when(instrumentService.getById(99L))
                                .thenThrow(new ResourceNotFoundException("Instrument not found with id: 99"));

                mockMvc.perform(get("/api/instruments/99"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Instrument not found with id: 99"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_returnsCreated_whenAdmin() throws Exception {
                when(instrumentService.create(any())).thenReturn(guitarResponse);

                mockMvc.perform(post("/api/instruments")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(guitarRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Yamaha Guitar"));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        void create_returns403_whenStudent() throws Exception {
                mockMvc.perform(post("/api/instruments")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(guitarRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void delete_returns204_whenAdmin() throws Exception {
                doNothing().when(instrumentService).delete(1L);

                mockMvc.perform(delete("/api/instruments/1").with(csrf()))
                                .andExpect(status().isNoContent());
        }
}
