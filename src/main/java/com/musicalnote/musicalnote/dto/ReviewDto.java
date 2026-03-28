package com.musicalnote.musicalnote.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class ReviewDto {

    @Data
    public static class Request {
        @NotNull
        private Long rentalId;
        @NotNull
        @Min(1)
        @Max(5)
        private Integer rating;
        private String comment;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private String userName;
        private Long instrumentId;
        private Long rentalId;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }
}
