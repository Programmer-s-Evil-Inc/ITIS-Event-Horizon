package ru.kpfu.itis.webapp.entity;

public enum AccountState {

    CONFIRMED, // Аккаунт подтвержден
    NOT_CONFIRMED, // Ожидает подтверждения
    DELETED, // Удаленный пользователь
    BANNED // Заблокирован администратором

}
