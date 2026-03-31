package com.musicalnote.musicalnote.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicalnote.musicalnote.dto.InstrumentDto;
import com.musicalnote.musicalnote.dto.RentalDto;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import com.musicalnote.musicalnote.enums.RentalStatus;
import com.musicalnote.musicalnote.enums.Role;
import com.musicalnote.musicalnote.exception.BusinessException;
import com.musicalnote.musicalnote.security.JwtFilter;
import com.musicalnote.musicalnote.service.RentalService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RentalController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
class RentalControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @MockBean
        private RentalService rentalService;

        private RentalDto.Response rentalResponse;
        private RentalDto.Request rentalRequest;

        @BeforeEach
        void setUp() {
                InstrumentDto.Response instrument = InstrumentDto.Response.builder()
                                .id(1L).name("Yamaha Guitar").category(Category.STRING)
                                .brand("Yamaha").condition(Condition.NEW)
                                .available(false).pricePerDay(15.0).build();

                rentalResponse = RentalDto.Response.builder()
                                .id(1L).userId(1L).userName("Jane Doe")
                                .instrument(instrument)
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
        @WithMockUser(roles = "STUDENT")
        void book_returnsCreated() throws Exception {
                when(rentalService.book(any(), any())).thenReturn(rentalResponse);

                mockMvc.perform(post("/api/rentals")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rentalRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.totalPrice").value(90.0));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        void book_returns400_whenInstrumentUnavailable() throws Exception {
                when(rentalService.book(any(), any()))
                                .thenThrow(new BusinessException("Instrument is not available for rental"));

                mockMvc.perform(post("/api/rentals")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rentalRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Instrument is not available for rental"));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        void getMyRentals_returnsList() throws Exception {
                when(rentalService.getMyRentals(any())).thenReturn(List.of(rentalResponse));

                mockMvc.perform(get("/api/rentals/my"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllRentals_returnsAll_whenAdmin() throws Exception {
                when(rentalService.getAllRentals()).thenReturn(List.of(rentalResponse));

                mockMvc.perform(get("/api/rentals"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        void getAllRentals_returns403_whenStudent() throws Exception {
                mockMvc.perform(get("/api/rentals"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void markReturned_returnsOk_whenAdmin() throws Exception {
                rentalResponse.setStatus(RentalStatus.RETURNED);
                when(rentalService.markReturned(1L)).thenReturn(rentalResponse);

                mockMvc.perform(put("/api/rentals/1/return").with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("RETURNED"));
        }
}
