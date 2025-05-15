package ru.kpfu.itis.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.webapp.entity.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    long countByOrganizerId(Long organizerId);
}
