package ru.kpfu.itis.webapp.telegram.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.kpfu.itis.webapp.dto.EventFilter;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.dto.TelegramEventDto;
import ru.kpfu.itis.webapp.service.EventService;
import ru.kpfu.itis.webapp.telegram.dto.LoginRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiClientService {

    private final EventService eventService;
    private final RestTemplate restTemplate;
    @Value("${server.url}")
    private String serverBaseUrl;

    public ApiClientService(EventService eventService, RestTemplate restTemplate) {
        this.eventService = eventService;
        this.restTemplate = restTemplate;
    }

    public List<TelegramEventDto> getAllEvents(EventFilter filter) {
        try {
            List<EventShortDto> shortEvents = eventService.getAllShortEvents(filter);
            return shortEvents.stream()
                    .map(this::convertToTelegramEventDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public String getEvents() {
        try {
            List<EventShortDto> events = eventService.getAllShortEvents(null);
            return events.isEmpty() ? "Нет событий" : events.toString();
        } catch (Exception e) {
            return "Ошибка получения событий";
        }
    }

    private TelegramEventDto convertToTelegramEventDto(EventShortDto event) {
        TelegramEventDto telegramEvent = new TelegramEventDto();
        telegramEvent.setId(event.id());
        telegramEvent.setTitle(event.title());
        telegramEvent.setDescription(event.description());
        telegramEvent.setDate(event.date());
        return telegramEvent;
    }

    public String login(String username, String password) {
        try {
            LoginRequest request = new LoginRequest(username, password);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    serverBaseUrl + "/api/auth/login",
                    request,
                    String.class
            );
            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getProfile(String token) {
        try {
            if (token == null) return "Нет токена авторизации";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    serverBaseUrl + "/api/profile",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : "Ошибка получения профиля";
        } catch (Exception e) {
            return "Ошибка получения профиля";
        }
    }
}
