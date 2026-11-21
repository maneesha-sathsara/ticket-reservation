package com.example.ticketreservation.dto;

public record UserResponse(

        String id,

        String username,

        String fullName,

        String role

) {
}