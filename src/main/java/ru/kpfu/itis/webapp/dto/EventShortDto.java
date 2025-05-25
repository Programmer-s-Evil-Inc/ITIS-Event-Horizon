package ru.kpfu.itis.webapp.dto;


import java.time.LocalDateTime;

public record EventShortDto(
        Long id,
        String title,
        LocalDateTime date,
        String description,
        String image_url
) {}
