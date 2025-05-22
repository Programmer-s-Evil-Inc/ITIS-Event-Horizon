package ru.kpfu.itis.webapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.kpfu.itis.webapp.entity.EventCategory;

import java.time.LocalDateTime;

@Data
public class EventCreationRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @Future(message = "Date must be in the future")
    private LocalDateTime date;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Participant limit is required")
    @PositiveOrZero
    private Integer participantLimit;

    @NotNull(message = "Category is required")
    private EventCategory category;

    @NotBlank(message = "Image UUID is required")
    private String imageUuid;
}
