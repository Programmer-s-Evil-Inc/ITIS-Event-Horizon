package ru.kpfu.itis.webapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.entity.Participation;
import ru.kpfu.itis.webapp.exceptions.DataNotFoundException;
import ru.kpfu.itis.webapp.repository.ParticipationRepository;
import ru.kpfu.itis.webapp.security.details.AccountUserDetails;
import ru.kpfu.itis.webapp.service.EventService;
import ru.kpfu.itis.webapp.service.impl.FileServiceMinioImpl;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/user/event")
@Tag(name = "User Event Controller", description = "Отвечает за один метод getSubscribedEvents")
public class UserEventController {

    private final EventService eventService;
    private final ParticipationRepository participationRepository;
    private final FileServiceMinioImpl fileService;

    @Operation(summary = "Мои события", description = "События, на которые подписан текущий пользователь")
    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'STUDENT')")
    public ResponseEntity<List<EventShortDto>> getSubscribedEvents(
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        return ResponseEntity.ok(eventService.getSubscribedEvents(userId));
    }

    @Operation(summary = "Валидация подписки", description = "Проверка подписки по QR-коду")
    @GetMapping("/valid")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> validateSubscription(
            @RequestParam Long subscriptionId
    ) {
        Participation participation = participationRepository.findById(subscriptionId)
                .orElseThrow(() -> new DataNotFoundException("Subscription not found"));

        return ResponseEntity.ok("Подписка действительна для пользователя: "
                + participation.getUser().getEmail());
    }

    @Operation(summary = "Получить QR-код подписки", description = "Доступно владельцу подписки")
    @GetMapping("/{eventId}/qrcode")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER')")
    public ResponseEntity<String> getSubscriptionQrCode(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AccountUserDetails userDetails
    ) {
        Long userId = userDetails.getAccount().getId();
        Participation participation = participationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new DataNotFoundException("Subscription not found for this event"));

        String fullQrCodeUrl = fileService.getBaseUrl() + participation.getQrCodeUid();
        return ResponseEntity.ok(fullQrCodeUrl);
    }

}
