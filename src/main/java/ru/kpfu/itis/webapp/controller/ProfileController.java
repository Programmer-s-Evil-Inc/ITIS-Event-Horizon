package ru.kpfu.itis.webapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kpfu.itis.webapp.dto.ProfileDto;
import ru.kpfu.itis.webapp.entity.Account;
import ru.kpfu.itis.webapp.security.details.AccountUserDetails;

@Controller
public class ProfileController {

    @GetMapping("/api/profile")
    public ResponseEntity<ProfileDto> getProfile(@AuthenticationPrincipal AccountUserDetails userDetails) {
        Account account = userDetails.getAccount();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setId(account.getId());
        profileDto.setEmail(account.getEmail());
        profileDto.setRole(account.getRole().name());
        profileDto.setPhotoUrl(account.getPhotoUrl());
        return ResponseEntity.ok(profileDto);
    }

}
