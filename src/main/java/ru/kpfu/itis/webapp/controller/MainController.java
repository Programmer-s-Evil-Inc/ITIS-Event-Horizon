package ru.kpfu.itis.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Пользователь");
        return "home";
    }

    @GetMapping("/sign-up")
    public String singUp() {
        return "sign-up";
    }

}
