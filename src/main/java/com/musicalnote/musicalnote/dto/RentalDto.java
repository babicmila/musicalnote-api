package com.musicalnote.musicalnote.dto;

import com.musicalnote.musicalnote.enums.RentalStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RentalDto {

    @Data
    public static class Request {
        @NotNull
        private Long instrumentId;
        @NotNull
        @Future
        private LocalDate startDate;
        @NotNull
        @Future
        private LocalDate endDate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private String userName;
        private InstrumentDto.Response instrument;
        private LocalDate startDate;
        private LocalDate endDate;
        private RentalStatus status;
        private Double totalPrice;
        private LocalDateTime createdAt;
    }
}
