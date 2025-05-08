package ru.kpfu.itis.webapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountRole role;  // STUDENT, ORGANIZER или GUEST

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountState state;  // CONFIRMED, NOT_CONFIRMED и т.д.
}