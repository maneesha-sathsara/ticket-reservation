package com.example.ticketreservation.dto;

import java.math.BigDecimal;

public record StatsResponse(

        long totalUsers,

        long totalEvents,

        long totalReservations,

        long confirmedReservations,

        long cancelledReservations,

        long totalTicketsSold,

        BigDecimal totalRevenue

) {
}