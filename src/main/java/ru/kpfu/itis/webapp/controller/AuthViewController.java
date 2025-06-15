package ru.kpfu.itis.webapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kpfu.itis.webapp.security.details.AccountUserDetails;

// HTML-контроллер (страницы)
@Controller
public class AuthViewController {
    @GetMapping("/login")
    public String loginPage() {
        return "sign-in";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal AccountUserDetails userDetails) {
        return "profile";
    }
}
