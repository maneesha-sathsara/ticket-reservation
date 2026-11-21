package com.example.ticketreservation.service;

import com.example.ticketreservation.dto.ReservationRequest;
import com.example.ticketreservation.dto.ReservationResponse;
import com.example.ticketreservation.exception.ApiException;
import com.example.ticketreservation.model.Event;
import com.example.ticketreservation.model.Reservation;
import com.example.ticketreservation.model.ReservationStatus;
import com.example.ticketreservation.repository.EventRepository;
import com.example.ticketreservation.repository.ReservationRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final MongoTemplate mongoTemplate;

    public ReservationService(
            ReservationRepository reservationRepository,
            EventRepository eventRepository,
            EventService eventService,
            MongoTemplate mongoTemplate) {

        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.mongoTemplate = mongoTemplate;
    }

    public ReservationResponse book(
            ReservationRequest request,
            String customerUsername) {

        Event event = eventService.findEvent(request.eventId());

        eventService.reserveSeats(event.getId(), request.seats());

        try {

            BigDecimal amount = event.getTicketPrice()
                    .multiply(BigDecimal.valueOf(request.seats()));

            String code = "TKT-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase();

            Reservation reservation = new Reservation(
                    code,
                    customerUsername,
                    event.getId(),
                    request.seats(),
                    amount,
                    ReservationStatus.CONFIRMED
            );

            Reservation saved =
                    reservationRepository.save(reservation);

            return toResponse(saved, event.getName());

        } catch (RuntimeException ex) {

            eventService.releaseSeats(
                    event.getId(),
                    request.seats()
            );

            throw ex;
        }
    }

    public List<ReservationResponse> getMyReservations(
            String username) {

        return reservationRepository
                .findByCustomerUsername(username)
                .stream()
                .map(this::toResponseWithEvent)
                .toList();
    }

    public List<ReservationResponse> getAllReservations() {

        return reservationRepository.findAll()
                .stream()
                .map(this::toResponseWithEvent)
                .toList();
    }

    public List<ReservationResponse> getOrganizerReservations(
            String organizer) {

        List<ReservationResponse> results = new ArrayList<>();

        for (Event event : eventRepository.findByOrganizer(organizer)) {

            List<Reservation> reservations =
                    reservationRepository.findByEventId(event.getId());

            for (Reservation reservation : reservations) {
                results.add(toResponse(reservation, event.getName()));
            }
        }

        return results;
    }

    public void cancel(
            String reservationId,
            String username,
            boolean isAdmin) {

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() ->
                        ApiException.notFound("Reservation not found")
                );

        if (!isAdmin &&
                !reservation.getCustomerUsername().equals(username)) {

            throw ApiException.forbidden(
                    "You cannot cancel another customer's reservation"
            );
        }

        Query query = new Query(
                Criteria.where("id").is(reservationId)
                        .and("status").is(ReservationStatus.CONFIRMED)
        );

        Update update = new Update()
                .set("status", ReservationStatus.CANCELLED);

        Reservation cancelled = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Reservation.class
        );

        if (cancelled == null) {
            throw ApiException.conflict(
                    "Reservation is already cancelled"
            );
        }

        eventService.releaseSeats(
                cancelled.getEventId(),
                cancelled.getSeats()
        );
    }

    private ReservationResponse toResponseWithEvent(
            Reservation reservation) {

        Event event = eventRepository.findById(reservation.getEventId())
                .orElse(null);

        String eventName = event == null
                ? "Deleted event"
                : event.getName();

        return toResponse(reservation, eventName);
    }

    private ReservationResponse toResponse(
            Reservation reservation,
            String eventName) {

        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationCode(),
                reservation.getEventId(),
                eventName,
                reservation.getSeats(),
                reservation.getAmount(),
                reservation.getStatus()
        );
    }
}