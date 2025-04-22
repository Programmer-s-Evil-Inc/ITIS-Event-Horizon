package ru.kpfu.itis.webapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.webapp.model.Account;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final List<Account> users = new ArrayList<>();

    @PostMapping("/register")
    public ResponseEntity<Account> register(@ RequestBody Account user) {
        user.setId((long) (users.size() + 1));
        users.add(user);
        return ResponseEntity.ok(user);
    }
}