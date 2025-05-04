package ru.kpfu.itis.webapp.dto;

import ru.kpfu.itis.webapp.entity.EventCategory;
import java.time.LocalDateTime;

public record EventFullDto(
        Long id,
        String title,
        String description,
        LocalDateTime date,
        String location,
        Integer participantLimit,
        Long organizerId,
        EventCategory category
) {}