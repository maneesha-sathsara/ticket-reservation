package com.example.ticketreservation.service;

import com.example.ticketreservation.dto.EventRequest;
import com.example.ticketreservation.dto.EventResponse;
import com.example.ticketreservation.exception.ApiException;
import com.example.ticketreservation.model.Event;
import com.example.ticketreservation.repository.EventRepository;
import com.example.ticketreservation.repository.ReservationRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final MongoTemplate mongoTemplate;

    public EventService(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            MongoTemplate mongoTemplate) {

        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public EventResponse createEvent(
            EventRequest request,
            String organizer) {

        Event event = new Event(
                request.name(),
                request.description(),
                request.venue(),
                request.eventDate(),
                request.totalSeats(),
                request.ticketPrice(),
                organizer
        );

        return toResponse(eventRepository.save(event));
    }

    public List<EventResponse> getAllEvents(String name) {

        List<Event> events;

        if (name == null || name.isBlank()) {
            events = eventRepository.findAll();
        } else {
            events = eventRepository.findByNameContainingIgnoreCase(name);
        }

        return events.stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEventById(String id) {

        Event event = findEvent(id);

        return toResponse(event);
    }

    public EventResponse updateEvent(
            String id,
            EventRequest request,
            String username,
            boolean isAdmin) {

        Event event = findEvent(id);

        if (!isAdmin && !event.getOrganizer().equals(username)) {
            throw ApiException.forbidden(
                    "You can only update your own events"
            );
        }

        int soldSeats = event.getTotalSeats()
                - event.getAvailableSeats();

        if (request.totalSeats() < soldSeats) {
            throw ApiException.conflict(
                    "Total seats cannot be less than already reserved seats"
            );
        }

        event.setName(request.name());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setEventDate(request.eventDate());
        event.setTotalSeats(request.totalSeats());
        event.setAvailableSeats(request.totalSeats() - soldSeats);
        event.setTicketPrice(request.ticketPrice());

        return toResponse(eventRepository.save(event));
    }

    public void deleteEvent(String id) {

        Event event = findEvent(id);

        if (reservationRepository.existsByEventId(id)) {
            throw ApiException.conflict(
                    "Cannot delete an event that has reservations"
            );
        }

        eventRepository.delete(event);
    }

    public List<Event> getEventsByOrganizer(String organizer) {
        return eventRepository.findByOrganizer(organizer);
    }

    public Event findEvent(String id) {

        return eventRepository.findById(id)
                .orElseThrow(() ->
                        ApiException.notFound("Event not found: " + id)
                );
    }

    // Atomically reserve seats to prevent double booking.
    public void reserveSeats(String eventId, int seats) {

        Query query = new Query(
                Criteria.where("id").is(eventId)
                        .and("availableSeats").gte(seats)
        );

        Update update = new Update()
                .inc("availableSeats", -seats);

        Event updatedEvent = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Event.class
        );

        if (updatedEvent == null) {
            throw ApiException.conflict(
                    "Event not found or insufficient available seats"
            );
        }
    }

    // Release seats when a reservation is cancelled.
    public void releaseSeats(String eventId, int seats) {

        Query query = new Query(
                Criteria.where("id").is(eventId)
                        .and("availableSeats").lte(
                                findEvent(eventId).getTotalSeats() - seats
                        )
        );

        Update update = new Update()
                .inc("availableSeats", seats);

        Event updatedEvent = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Event.class
        );

        if (updatedEvent == null) {
            throw ApiException.conflict(
                    "Unable to release reserved seats"
            );
        }
    }

    private EventResponse toResponse(Event event) {

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getVenue(),
                event.getEventDate(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getTicketPrice(),
                event.getOrganizer()
        );
    }
}