package com.example.ticketreservation.dto;

import com.example.ticketreservation.model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50,
              message = "Username must contain 3-50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100,
              message = "Password must contain at least 8 characters")
        String password,

        @NotBlank(message = "Full name is required")
        @Size(max = 100,
              message = "Full name cannot exceed 100 characters")
        String fullName,

        @NotNull(message = "Role is required")
        Role role

) {
}