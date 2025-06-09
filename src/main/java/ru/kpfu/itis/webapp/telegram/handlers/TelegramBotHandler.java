package ru.kpfu.itis.webapp.telegram.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kpfu.itis.webapp.dto.EventShortDto;
import ru.kpfu.itis.webapp.dto.TelegramEventDto;
import ru.kpfu.itis.webapp.entity.Account;
import ru.kpfu.itis.webapp.telegram.model.TelegramUser;
import ru.kpfu.itis.webapp.telegram.repository.AccountRepositoryTelegram;
import ru.kpfu.itis.webapp.telegram.repository.TelegramUserRepository;
import ru.kpfu.itis.webapp.telegram.service.ApiClientService;
import ru.kpfu.itis.webapp.telegram.service.TelegramEventService;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
public class TelegramBotHandler extends TelegramLongPollingBot{

    private static TelegramBotHandler instance;
    private final ApiClientService apiClientService;
    private final TelegramUserRepository userRepository;
    private static final String BOT_USERNAME = "testItissBot";
    private String botToken;

    @Value("${telegram.bot.token}")
    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    @Autowired
    private AccountRepositoryTelegram accountRepository;
    @Autowired
    private TelegramEventService telegramEventService;

    public TelegramBotHandler(ApiClientService apiClientService, TelegramUserRepository userRepository) {
        this.apiClientService = apiClientService;
        this.userRepository = userRepository;
        instance = this;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            String[] parts = messageText.split("\\s+");
            String command = parts[0];
            String[] args = parts.length > 1
                    ? Arrays.copyOfRange(parts, 1, parts.length)
                    : new String[0];

            switch (command) {
                case "/start":
                    sendMessage(chatId, "Привет! Используй команды:\n" +
                            "/login [username] [password] - авторизация\n" +
                            "/events - список мероприятий, на которые ты записан\n" +
                            "/allevents - список доступных мероприятий\n" +
                            "/profile - твой профиль\n" +
                            "/subscribe <ID мероприятия> - зарегистрироваться на мероприятие");
                    break;
                case "/events":
                    handleEventsCommand(chatId);
                    break;
                case "/allevents":
                    allEvents(chatId);
                    break;
                case "/profile":
                    handleProfileCommand(chatId);
                    break;
                case "/subscribe":
                    handleSubscribeCommand(chatId, args);
                    break;
                default:
                    if (messageText.startsWith("/login")) {
                        handleLoginCommand(chatId, messageText);
                    }
                    break;
            }
        }
    }

    private void handleLoginCommand(long chatId, String message) {
        String[] parts = message.split(" ");
        if (parts.length != 3) {
            sendMessage(chatId, "Ошибка! Формат: /login username password");
            return;
        }
        String username = parts[1];
        String password = parts[2];
        String token = apiClientService.login(username, password);
        if (token != null) {
            Optional<TelegramUser> optionalUser = userRepository.findByChatId(chatId);
            TelegramUser user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                user.setUsername(username);
                user.setToken(token);
            } else {
                user = new TelegramUser(chatId, username, token);
            }
            accountRepository.findByEmail(username).ifPresent(user::setAccount);
            userRepository.save(user);
            sendMessage(chatId, "✅ Авторизация успешна! Токен сохранен");
        } else {
            sendMessage(chatId, "❌ Ошибка авторизации");
        }
    }

    private void handleProfileCommand(long chatId) {
        Optional<TelegramUser> optionalUser = userRepository.findByChatId(chatId);
        if (optionalUser.isPresent()) {
            TelegramUser user = optionalUser.get();

            Optional<Account> optionalAccount = accountRepository.findByEmail(user.getUsername());

            String role;
            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                role = account.getRole().toString();
            } else {
                role = "Роль не найдена";
            }
            String profileInfo = "👤 Ваш профиль:\n" +
                    "Username: " + user.getUsername() + "\n" +
                    "Role: " + role + "\n";

            sendMessage(chatId, profileInfo);
        } else {
            sendMessage(chatId, "Вы не авторизованы. Используйте /login [username] [password]");
        }
    }



    private void handleEventsCommand(long chatId) {
        Optional<TelegramUser> optionalUser = userRepository.findByChatId(chatId);
        if (optionalUser.isPresent()) {
            TelegramUser user = optionalUser.get();
            Long userId = user.getSystemUserId();
            List<EventShortDto> events = telegramEventService.getEventsForTelegramUser(userId);
            if (events.isEmpty()) {
                sendMessage(chatId, "📭 У вас нет зарегистрированных мероприятий.");
            } else {
                StringBuilder builder = new StringBuilder("📅 Мероприятия, на которые ты записан:\n\n");
                for (EventShortDto event : events) {
                    builder.append("🔹 ")
                            .append(event.title())
                            .append(" — ")
                            .append(event.date().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                            .append("\n");
                }
                sendMessage(chatId, builder.toString());

            }
        } else {
            sendMessage(chatId, "❌ Сначала авторизуйтесь с помощью /login [username] [password]");
        }
    }

    private void allEvents(long chatId) {
        try {
            List<TelegramEventDto> events = apiClientService.getAllEvents(null);

            if (events == null || events.isEmpty()) {
                sendMessage(chatId, "📭 На данный момент нет доступных мероприятий");
                return;
            }

            StringBuilder builder = new StringBuilder();
            builder.append("🎉 *Доступные мероприятия:*\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (TelegramEventDto event : events) {
                builder.append("📍 *").append(event.getTitle()).append("*\n");
                builder.append("🕒 ").append(event.getDate().format(formatter)).append("\n");
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    String description = event.getDescription();
                    if (description.length() > 100) {
                        description = description.substring(0, 97) + "...";
                    }
                    builder.append("📝 ").append(description).append("\n");
                }

                builder.append("🔹 ID для записи: ").append(event.getId()).append("\n\n");
            }

            builder.append("\nℹ️ Для записи используй /subscribe [ID]");
            sendMessage(chatId, builder.toString());

        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Ошибка при получении мероприятий: " + e.getMessage());
        }
    }

    private void handleSubscribeCommand(long chatId, String[] args) {
        if (args.length < 1) {
            sendMessage(chatId, "⚠️ Используйте команду так: /subscribe <ID_мероприятия>");
            return;
        }

        Optional<TelegramUser> optionalUser = userRepository.findByChatId(chatId);
        if (optionalUser.isEmpty()) {
            sendMessage(chatId, "❌ Сначала авторизуйтесь с помощью /login [username] [password]");
            return;
        }
        TelegramUser telegramUser = optionalUser.get();

        Long userId = telegramUser.getSystemUserId();

        Long eventId;
        try {
            eventId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ ID мероприятия должен быть числом.");
            return;
        }

        try {
            List<EventShortDto> subscribedEvents = telegramEventService.getEventsForTelegramUser(userId);
            boolean isAlreadySubscribed = subscribedEvents.stream()
                    .anyMatch(event -> event.id().equals(eventId));

            if (isAlreadySubscribed) {
                sendMessage(chatId, "⚠️ Вы уже подписаны на это мероприятие!");
                return;
            }
            String qrCodeUrl = telegramEventService.subscribeUserToEvent(chatId, eventId);
            sendMessage(chatId, "✅ Вы успешно подписались на мероприятие!");
        } catch (Exception e) {
            sendMessage(chatId, "🚫 Ошибка подписки: " + e.getMessage());
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPhoto(long chatId, String photoUrl, String caption) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setPhoto(new InputFile(photoUrl));
        photo.setCaption(caption);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "❌ Ошибка отправки фото.");
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000) // Проверять каждую минуту
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<TelegramUser> users = userRepository.findAll();

        for (TelegramUser user : users) {
            Long userId = user.getSystemUserId();
            if (userId != null) {
                List<EventShortDto> subscribedEvents = telegramEventService.getEventsForTelegramUser(userId);
                for (EventShortDto event : subscribedEvents) {
                    LocalDateTime eventTime = event.date();
                    Duration timeUntilEvent = Duration.between(now, eventTime);

                    long minutesUntilEvent = timeUntilEvent.toMinutes();
                    long hoursUntilEvent = timeUntilEvent.toHours();
                    long remainingMinutes = minutesUntilEvent % 60;

                    if (minutesUntilEvent == 60 || minutesUntilEvent == 30 || minutesUntilEvent == 10) {
                        String reminderMessage;
                        if (hoursUntilEvent == 0) {
                            reminderMessage = String.format(
                                    "⏰ Напоминание! Мероприятие '%s' начнется через %d %s.\nВремя: %s",
                                    event.title(),
                                    remainingMinutes,
                                    (remainingMinutes == 1 ? "минута" : "минут"),
                                    eventTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                            );
                        } else {
                            reminderMessage = String.format(
                                    "⏰ Напоминание! Мероприятие '%s' начнется через %d %s и %d %s.\nВремя: %s",
                                    event.title(),
                                    hoursUntilEvent,
                                    (hoursUntilEvent == 1 ? "час" : "часов"),
                                    remainingMinutes,
                                    (remainingMinutes == 1 ? "минута" : "минут"),
                                    eventTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                            );
                        }
                        sendMessage(user.getChatId(), reminderMessage);
                    }
                }
            }
        }
    }
}
