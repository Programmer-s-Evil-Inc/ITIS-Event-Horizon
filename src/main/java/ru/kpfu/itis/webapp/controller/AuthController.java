package ru.kpfu.itis.webapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.webapp.security.details.AccountUserDetailsService;
import ru.kpfu.itis.webapp.security.jwt.JwtUtil;
import ru.kpfu.itis.webapp.telegram.dto.LoginRequest;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AccountUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AccountUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Пришёл username: " + request.getUsername());
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                String token = jwtUtil.generateToken(userDetails.getUsername());
                return ResponseEntity.ok(new JwtResponse(token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный пароль");
            }
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден");
        }
    }

    public static class JwtResponse {
        private String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
