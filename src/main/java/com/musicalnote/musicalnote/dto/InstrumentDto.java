package com.musicalnote.musicalnote.dto;

import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

public class InstrumentDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        @NotNull
        private Category category;
        @NotBlank
        private String brand;
        private String description;
        private String imageUrl;
        @NotNull
        private Condition condition;
        @NotNull
        private Boolean available;
        @NotNull
        @Positive
        private Double pricePerDay;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private Category category;
        private String brand;
        private String description;
        private String imageUrl;
        private Condition condition;
        private boolean available;
        private Double pricePerDay;
    }
}
