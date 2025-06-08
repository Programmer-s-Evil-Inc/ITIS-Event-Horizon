package ru.kpfu.itis.webapp.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.service.EventService;
import ru.kpfu.itis.webapp.telegram.model.TelegramUser;
import ru.kpfu.itis.webapp.telegram.repository.TelegramUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramEventService {

    private final EventService eventService;
    private final TelegramUserRepository telegramUserRepository;

    public List<EventShortDto> getEventsForTelegramUser(Long userId) {
        return eventService.getSubscribedEvents(userId);
    }

    public String subscribeUserToEvent(Long chatId, Long eventId) {
        TelegramUser user = telegramUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        Long internalUserId = user.getSystemUserId();
        return eventService.subscribeToEvent(internalUserId, eventId);
    }
}

