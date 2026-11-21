package com.example.ticketreservation.service;

import com.example.ticketreservation.dto.StatsResponse;
import com.example.ticketreservation.model.Reservation;
import com.example.ticketreservation.model.ReservationStatus;
import com.example.ticketreservation.repository.EventRepository;
import com.example.ticketreservation.repository.ReservationRepository;
import com.example.ticketreservation.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public AdminService(
            UserRepository userRepository,
            EventRepository eventRepository,
            ReservationRepository reservationRepository) {

        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
    }

    public StatsResponse getStatistics() {

        long totalUsers = userRepository.count();

        long totalEvents = eventRepository.count();

        long totalReservations = reservationRepository.count();

        long confirmed = reservationRepository.countByStatus(
                ReservationStatus.CONFIRMED
        );

        long cancelled = reservationRepository.countByStatus(
                ReservationStatus.CANCELLED
        );

        List<Reservation> confirmedReservations =
                reservationRepository.findAll()
                        .stream()
                        .filter(r -> r.getStatus()
                                == ReservationStatus.CONFIRMED)
                        .toList();

        long totalTicketsSold = confirmedReservations.stream()
                .mapToLong(Reservation::getSeats)
                .sum();

        BigDecimal totalRevenue = confirmedReservations.stream()
                .map(Reservation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StatsResponse(
                totalUsers,
                totalEvents,
                totalReservations,
                confirmed,
                cancelled,
                totalTicketsSold,
                totalRevenue
        );
    }
}