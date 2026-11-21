package com.example.ticketreservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50,
              message = "Username must contain 3-50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(max = 100,
              message = "Password is too long")
        String password

) {
}