package ru.kpfu.itis.webapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId) {
        return eventService.getFullEventById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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