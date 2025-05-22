package ru.kpfu.itis.webapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.webapp.dto.EventCreationRequest;
import ru.kpfu.itis.webapp.dto.EventFilter;
import ru.kpfu.itis.webapp.dto.EventFullDto;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.security.details.AccountUserDetails;
import ru.kpfu.itis.webapp.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Tag(name = "Event Controller", description = "Управление событиями: создание, подписки, просмотр")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "Список событий", description = "Получить все события (краткая информация)")
    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAllEvents(@ModelAttribute EventFilter filter) {
        return ResponseEntity.ok(eventService.getAllShortEvents(filter));
    }

    @Operation(summary = "Создать событие", description = "Доступно организаторам")
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> createEvent(
            @RequestBody @Valid EventCreationRequest request,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long organizerId = userDetails.getAccount().getId();
        eventService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Детали события", description = "Полная информация о событии по ID")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId) {
        return eventService.getFullEventById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Подписаться на событие", description = "Доступно студентам и организаторам")
    @PostMapping("/{eventId}/subscribe")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER')")
    public ResponseEntity<Void> subscribeToEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        eventService.subscribeToEvent(userId, eventId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Мои события", description = "События, на которые подписан текущий пользователь")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'STUDENT')")
    public ResponseEntity<List<EventShortDto>> getSubscribedEvents(
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        return ResponseEntity.ok(eventService.getSubscribedEvents(userId));
    }

    @Operation(summary = "События организатора", description = "События, созданные текущим организатором")
    @GetMapping("/organizer")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<EventShortDto>> getOrganizerEvents(
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long organizerId = userDetails.getAccount().getId();
        return ResponseEntity.ok(eventService.getEventsByOrganizer(organizerId));
    }
}