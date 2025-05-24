package ru.kpfu.itis.webapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.webapp.dto.ProfileDto;
import ru.kpfu.itis.webapp.entity.Account;
import ru.kpfu.itis.webapp.security.details.AccountUserDetails;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile Controller", description = "Управление профилем пользователя")
public class ProfileController {

    @Operation(summary = "Получить профиль", description = "Данные текущего пользователя")
    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(@AuthenticationPrincipal AccountUserDetails userDetails) {
        Account account = userDetails.getAccount();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setId(account.getId());
        profileDto.setEmail(account.getEmail());
        profileDto.setRole(account.getRole().name());
        profileDto.setPhotoUrl(account.getPhotoUid());
        return ResponseEntity.ok(profileDto);
    }

}
