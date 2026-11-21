package com.example.ticketreservation.config;

import com.example.ticketreservation.model.AppUser;
import com.example.ticketreservation.model.Event;
import com.example.ticketreservation.model.Role;

import com.example.ticketreservation.repository.UserRepository;
import com.example.ticketreservation.repository.EventRepository;

import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(
            UserRepository userRepository,
            EventRepository eventRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        createUser(
                "admin",
                "admin123",
                "System Administrator",
                Role.ADMIN
        );

        createUser(
                "organizer",
                "organizer123",
                "Event Organizer",
                Role.ORGANIZER
        );

        createUser(
                "customer",
                "customer123",
                "Test Customer",
                Role.CUSTOMER
        );

        if (eventRepository.count() == 0) {

            Event event = new Event(
                    "Sample Concert",
                    "Sample event for Lab Sheet 08",
                    "Colombo",
                    LocalDateTime.now().plusDays(30),
                    100,
                    new BigDecimal("1000.00"),
                    "organizer"
            );

            eventRepository.save(event);
        }
    }

    private void createUser(
            String username,
            String password,
            String fullName,
            Role role) {

        if (!userRepository.existsByUsername(username)) {

            AppUser user = new AppUser(
                    username,
                    passwordEncoder.encode(password),
                    fullName,
                    role
            );

            userRepository.save(user);
        }
    }
}