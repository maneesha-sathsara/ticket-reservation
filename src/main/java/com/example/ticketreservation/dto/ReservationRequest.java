package com.example.ticketreservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReservationRequest(

        @NotBlank(message = "Event ID is required")
        String eventId,

        @Min(value = 1,
             message = "Minimum reservation is 1 seat")
        @Max(value = 10,
             message = "Maximum reservation is 10 seats")
        int seats

) {
}