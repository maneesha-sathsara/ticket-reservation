package com.example.ticketreservation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventRequest(

        @NotBlank(message = "Event name is required")
        @Size(max = 150,
              message = "Event name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "Description is required")
        @Size(max = 1000,
              message = "Description cannot exceed 1000 characters")
        String description,

        @NotBlank(message = "Venue is required")
        @Size(max = 200,
              message = "Venue cannot exceed 200 characters")
        String venue,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be in the future")
        LocalDateTime eventDate,

        @Min(value = 1,
             message = "Total seats must be at least 1")
        int totalSeats,

        @NotNull(message = "Ticket price is required")
        @DecimalMin(value = "0.0",
                    message = "Ticket price cannot be negative")
        BigDecimal ticketPrice

) {
}