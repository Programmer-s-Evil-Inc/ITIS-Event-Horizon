package ru.kpfu.itis.webapp.telegram.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import ru.kpfu.itis.webapp.entity.Account;

@Entity
public class TelegramUser {

    @Id
    private Long chatId;

    private String username;
    private String token;

    public TelegramUser() {}

    public TelegramUser(Long chatId, String username, String token) {
        this.chatId = chatId;
        this.username = username;
        this.token = token;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @OneToOne
    @JoinColumn(name = "id") // предполагаем, что в таблице есть столбец account_id
    private Account account;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Long getSystemUserId() {
        return account != null ? account.getId() : null;
    }

}

