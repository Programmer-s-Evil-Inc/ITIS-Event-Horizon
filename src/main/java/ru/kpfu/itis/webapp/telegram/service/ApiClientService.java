package ru.kpfu.itis.webapp.telegram.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.kpfu.itis.webapp.dto.TelegramEventDto;
import ru.kpfu.itis.webapp.telegram.dto.LoginRequest;

import java.util.List;

@Service
public class ApiClientService {
    private static final String BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate;

    public ApiClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/api/auth/login",
                request,
                String.class
        );
        return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
    }

    public String getEvents() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/api/events",
                String.class
        );
        return response.getBody();
    }

    public List<TelegramEventDto> getAllEvents() {
        ResponseEntity<List<TelegramEventDto>> response = restTemplate.exchange(
                BASE_URL + "/api/events",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TelegramEventDto>>() {}
        );
        return response.getBody();
    }

    public String getProfile(String token) {
        if (token == null) return "Нет токена авторизации";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/api/profile",
                HttpMethod.GET,
                entity,
                String.class
        );
        return response.getStatusCode() == HttpStatus.OK ? response.getBody() : "Ошибка получения профиля";
    }
}
