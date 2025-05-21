package ru.kpfu.itis.webapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final AccountRepository accountRepository;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.bucket}")
    private String bucketName;

    public List<EventShortDto> getAllShortEvents(EventFilter filter) {
        List<Event> events;
        if (filter.getTitle() != null && !filter.getTitle().isEmpty()) {
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
                event.getLocation()
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
                event.getImageUrl()
        );
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
    public void createEvent(EventCreationRequest request, Long organizerId) {
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
                .imageUrl(constructImageUrl(request.getImageUuid()))
                .build();

        eventRepository.save(event);
    }

    private String constructImageUrl(String imageUuid) {
        return String.format("%s/%s/events/images/%s",
                minioEndpoint,
                bucketName,
                imageUuid);
    }

    @Transactional
    public void subscribeToEvent(Long userId, Long eventId) {
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
    }

}
