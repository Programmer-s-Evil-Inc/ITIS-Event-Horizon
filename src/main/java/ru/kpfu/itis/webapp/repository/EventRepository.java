package ru.kpfu.itis.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.webapp.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {}
