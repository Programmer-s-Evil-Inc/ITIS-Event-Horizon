package ru.kpfu.itis.webapp.telegram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.webapp.telegram.repository.AccountRepositoryTelegram;

@Service
public class AccountService {

    private final AccountRepositoryTelegram accountRepository;

    @Autowired
    public AccountService(AccountRepositoryTelegram accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String getRoleByEmail(String email) {
        return accountRepository.findByEmail(email)
                .map(account -> account.getRole().toString())
                .orElse("Роль не найдена");
    }
}

