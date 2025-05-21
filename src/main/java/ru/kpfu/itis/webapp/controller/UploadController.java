package ru.kpfu.itis.webapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.service.FileService;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload Controller", description = "Загрузка изображений для событий")
public class UploadController {
    private final FileService fileService;

    @Operation(summary = "Загрузить изображение", description = "Доступно организаторам. Поддерживаются PNG/JPEG.")
    @PostMapping("/event")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> uploadEventImage(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only images are allowed");
            }

            String uuid = UUID.randomUUID().toString();
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String objectName = "events/images/" + uuid + "." + extension;

            String fileUrl = fileService.uploadFile(file, objectName);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
