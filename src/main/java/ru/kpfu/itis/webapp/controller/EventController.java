package ru.kpfu.itis.webapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.dto.EventCreationRequest;
import ru.kpfu.itis.webapp.dto.EventFilter;
import ru.kpfu.itis.webapp.dto.EventFullDto;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.entity.Participation;
import ru.kpfu.itis.webapp.exceptions.DataNotFoundException;
import ru.kpfu.itis.webapp.repository.ParticipationRepository;
import ru.kpfu.itis.webapp.security.details.AccountUserDetails;
import ru.kpfu.itis.webapp.service.EventService;
import ru.kpfu.itis.webapp.service.FileService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Tag(name = "Event Controller", description = "Управление событиями: создание, подписки, просмотр")
public class EventController {
    private final EventService eventService;
    private final ParticipationRepository participationRepository;
    private final FileService fileService;

    @Operation(summary = "Список событий", description = "Получить все события (краткая информация)")
    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAllEvents(@ModelAttribute EventFilter filter) {
        return ResponseEntity.ok(eventService.getAllShortEvents(filter));
    }

    @Operation(summary = "Создать событие", description = "Доступно организаторам. Поддерживаются PNG/JPEG/JPG.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> createEvent(
            @ModelAttribute @Valid EventCreationRequest request,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        if (image.isEmpty()) {
            throw new ServiceException("File is empty");
        }

        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ServiceException("Invalid file name");
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !Set.of("png", "jpg", "jpeg").contains(extension.toLowerCase())) {
            throw new ServiceException("Unsupported image format");
        }

        String uuid = UUID.randomUUID().toString();
        String imageUid = "events/images/" + uuid + "." + extension;

        try {
            fileService.uploadFile(image, imageUid);
        } catch (Exception e) {
            throw new ServiceException("Upload failed: " + e.getMessage());
        }

        Long organizerId = userDetails.getAccount().getId();
        eventService.createEvent(request, organizerId, imageUid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Детали события", description = "Полная информация о событии по ID")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId) {
        return eventService.getFullEventById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Удалить событие", description = "Доступно организатору события")
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long organizerId = userDetails.getAccount().getId();
        eventService.deleteEvent(eventId, organizerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Подписаться на событие", description = "Доступно студентам и организаторам")
    @PostMapping("/{eventId}/subscribe")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER')")
    public ResponseEntity<String> subscribeToEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        String qrCodeUrl = eventService.subscribeToEvent(userId, eventId);
        return ResponseEntity.ok(qrCodeUrl);
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

    @Operation(summary = "Мои подписки", description = "События, на которые подписан текущий пользователь")
    @GetMapping("/subscriptions")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'STUDENT')")
    public ResponseEntity<List<EventShortDto>> getSubscribedEvents(
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        return ResponseEntity.ok(eventService.getSubscribedEvents(userId));
    }

    @Operation(summary = "Валидация подписки", description = "Проверка подписки по ID (для организаторов)")
    @GetMapping("/subscriptions/validate")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> validateSubscription(
            @RequestParam Long subscriptionId
    ) {
        Participation participation = participationRepository.findById(subscriptionId)
                .orElseThrow(() -> new DataNotFoundException("Подписка не найдена"));
        return ResponseEntity.ok("Подписка действительна для пользователя: " + participation.getUser().getEmail());
    }

    @Operation(summary = "Получить QR-код подписки", description = "Доступно владельцу подписки")
    @GetMapping("/subscriptions/{eventId}/qrcode")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER')")
    public ResponseEntity<String> getSubscriptionQrCode(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new DataNotFoundException("Подписка не найдена"));

        if (!participation.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Нет доступа к этой подписке");
        }

        String fullQrCodeUrl = fileService.getBaseUrl() + participation.getQrCodeUid();
        return ResponseEntity.ok(fullQrCodeUrl);
    }

}