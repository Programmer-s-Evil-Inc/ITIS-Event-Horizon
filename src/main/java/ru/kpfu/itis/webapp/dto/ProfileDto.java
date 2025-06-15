package ru.kpfu.itis.webapp.dto;

import lombok.Data;

@Data
public class ProfileDto {
    private Long id;
    private String email;
    private String role;
    private String photoUrl;
}
