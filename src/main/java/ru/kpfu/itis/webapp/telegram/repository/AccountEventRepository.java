package ru.kpfu.itis.webapp.telegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.webapp.entity.Account;
import ru.kpfu.itis.webapp.entity.AccountEvent;
import ru.kpfu.itis.webapp.entity.Event;

public interface AccountEventRepository extends JpaRepository<AccountEvent, Long> {
    boolean existsByAccountAndEvent(Account account, Event event);
}
