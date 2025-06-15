package ru.kpfu.itis.webapp.telegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.webapp.telegram.model.TelegramUser;

import java.util.Optional;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Optional<TelegramUser> findByChatId(Long chatId);
}
