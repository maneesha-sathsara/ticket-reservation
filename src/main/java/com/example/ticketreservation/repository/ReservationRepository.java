package com.example.ticketreservation.repository;

import com.example.ticketreservation.model.Reservation;
import com.example.ticketreservation.model.ReservationStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReservationRepository
        extends MongoRepository<Reservation, String> {

    // Get reservations made by a specific customer
    List<Reservation> findByCustomerUsername(String customerUsername);

    // Get reservations for a specific event
    List<Reservation> findByEventId(String eventId);

    // Check whether an event has reservations
    boolean existsByEventId(String eventId);

    // Count reservations by status
    long countByStatus(ReservationStatus status);

    // Count reservations made by a customer
    long countByCustomerUsername(String customerUsername);
}