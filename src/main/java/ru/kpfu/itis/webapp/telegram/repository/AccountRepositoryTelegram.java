package ru.kpfu.itis.webapp.telegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.itis.webapp.entity.Account;

import java.util.Optional;

@Repository
public interface AccountRepositoryTelegram extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
}

