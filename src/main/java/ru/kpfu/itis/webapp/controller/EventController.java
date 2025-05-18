package ru.kpfu.itis.webapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.webapp.dto.EventCreationRequest;
import ru.kpfu.itis.webapp.dto.EventFullDto;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.security.details.AccountUserDetails;
import ru.kpfu.itis.webapp.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/event")
    public ResponseEntity<List<EventShortDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllShortEvents());
    }

    @PostMapping("/event")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> createEvent(
            @RequestBody @Valid EventCreationRequest request,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long organizerId = userDetails.getAccount().getId();
        eventService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId) {
        return eventService.getFullEventById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/event/{eventId}/subscribe")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER')")
    public ResponseEntity<Void> subscribeToEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        eventService.subscribeToEvent(userId, eventId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'STUDENT')")
    public ResponseEntity<List<EventShortDto>> getSubscribedEvents(
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        return ResponseEntity.ok(eventService.getSubscribedEvents(userId));
    }

    @GetMapping("/organizer/events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<EventShortDto>> getOrganizerEvents(
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long organizerId = userDetails.getAccount().getId();
        return ResponseEntity.ok(eventService.getEventsByOrganizer(organizerId));
    }
}