package ru.kpfu.itis.webapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

// entity/AccountEvent.java
@Entity
public class AccountEvent {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Event event;

    private LocalDateTime subscribedAt;
}

