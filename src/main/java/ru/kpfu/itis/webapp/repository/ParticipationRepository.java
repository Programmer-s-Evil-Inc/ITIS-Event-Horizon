package ru.kpfu.itis.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.webapp.entity.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByUserId(Long userId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    long countByEventId(Long eventId);
    Optional<Participation> findByUserIdAndEventId(Long userId, Long eventId);

}
