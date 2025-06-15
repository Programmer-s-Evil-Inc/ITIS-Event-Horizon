package ru.kpfu.itis.webapp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.dto.EventCreationRequest;
import ru.kpfu.itis.webapp.dto.EventFilter;
import ru.kpfu.itis.webapp.dto.EventFullDto;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.entity.Account;
import ru.kpfu.itis.webapp.entity.Event;
import ru.kpfu.itis.webapp.entity.Participation;
import ru.kpfu.itis.webapp.exceptions.DataNotFoundException;
import ru.kpfu.itis.webapp.exceptions.ServiceException;
import ru.kpfu.itis.webapp.repository.AccountRepository;
import ru.kpfu.itis.webapp.repository.EventRepository;
import ru.kpfu.itis.webapp.repository.ParticipationRepository;
import ru.kpfu.itis.webapp.utils.ByteArrayMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final AccountRepository accountRepository;
    private final FileService fileService;
    @Value("${server.url}")
    private String serverBaseUrl;

    public List<EventShortDto> getAllShortEvents(EventFilter filter) {
        List<Event> events;
        if (filter != null && filter.getTitle() != null && !filter.getTitle().isEmpty()) {
            events = eventRepository.findByTitleContainingIgnoreCase(filter.getTitle());
        } else {
            events = eventRepository.findAll();
        }
        return events.stream()
                .map(this::convertToShortDto)
                .toList();
    }

    private EventShortDto convertToShortDto(Event event) {
        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getDate(),
                event.getDescription(),
                buildImageUrl(event.getImageUid())
        );
    }

    public Optional<EventFullDto> getFullEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(this::convertToFullDto);
    }

    private EventFullDto convertToFullDto(Event event) {
        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getParticipantLimit(),
                event.getOrganizerId(),
                event.getCategory(),
                buildImageUrl(event.getImageUid())
        );
    }

    private String buildImageUrl(String imageUid) {
        return fileService.getBaseUrl() + imageUid;
    }

    public List<EventShortDto> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::convertToShortDto)
                .toList();
    }

    public List<EventShortDto> getSubscribedEvents(Long userId) {
        return participationRepository.findByUserId(userId).stream()
                .map(Participation::getEvent)
                .map(this::convertToShortDto)
                .toList();
    }

    @Transactional
    public void createEvent(EventCreationRequest request, Long organizerId, String imageUid) {
        if (eventRepository.countByOrganizerId(organizerId) >= 10) {
            throw new IllegalStateException("Event limit reached");
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .location(request.getLocation())
                .participantLimit(request.getParticipantLimit())
                .organizerId(organizerId)
                .category(request.getCategory())
                .imageUid(imageUid)
                .build();

        eventRepository.save(event);
    }

    @Transactional
    public String subscribeToEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DataNotFoundException("Event not found"));

        Account user = accountRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        if (participationRepository.existsByUserIdAndEventId(userId, eventId)) {
            log.warn("User {} already subscribed to event {}", userId, eventId);
            throw new ServiceException("Already subscribed");
        }

        if (event.getParticipantLimit() != null &&
                participationRepository.countByEventId(eventId) >= event.getParticipantLimit()) {
            log.warn("Participant limit reached for event {}", eventId);
            throw new ServiceException("Participant limit reached");
        }

        Participation participation = new Participation();
        participation.setUser(user);
        participation.setEvent(event);
        participationRepository.save(participation);

        String qrCodeUid = generateAndUploadQrCode(participation.getId());
        participation.setQrCodeUid(qrCodeUid);
        participationRepository.save(participation);

        return fileService.getBaseUrl() + qrCodeUid;
    }

    private String generateAndUploadQrCode(Long subscriptionId) {
        try {
            String qrCodeText = serverBaseUrl + "/api/events/subscriptions/validate?subscriptionId=" + subscriptionId;
            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrCodeText, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            String objectName = "subscriptions/qrcodes/" + subscriptionId + ".png";
            MultipartFile qrCodeFile = new ByteArrayMultipartFile(qrCodeBytes, objectName);
            fileService.uploadFile(qrCodeFile, objectName);
            return objectName;
        } catch (Exception e) {
            throw new ServiceException("Ошибка генерации QR-кода");
        }
    }

    @Transactional
    public void deleteEvent(Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DataNotFoundException("Event not found"));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new AccessDeniedException("Only event organizer can delete it");
        }

        List<Participation> participations = participationRepository.findByEventId(eventId);

        for (Participation participation : participations) {
            if (participation.getQrCodeUid() != null) {
                fileService.deleteFile(participation.getQrCodeUid());
            }
            participationRepository.delete(participation);
        }

        if (event.getImageUid() != null) {
            fileService.deleteFile(event.getImageUid());
        }

        eventRepository.delete(event);
    }
}
