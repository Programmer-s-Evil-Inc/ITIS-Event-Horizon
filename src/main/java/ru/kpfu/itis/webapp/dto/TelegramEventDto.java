package ru.kpfu.itis.webapp.dto;

import java.time.LocalDateTime;

public class TelegramEventDto {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
