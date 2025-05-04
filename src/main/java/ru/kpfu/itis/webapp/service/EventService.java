package ru.kpfu.itis.webapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.webapp.dto.EventFullDto;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.entity.Event;
import ru.kpfu.itis.webapp.repository.EventRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<EventShortDto> getAllShortEvents() {
        return eventRepository.findAll().stream()
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
                event.getCategory()
        );
    }

}
