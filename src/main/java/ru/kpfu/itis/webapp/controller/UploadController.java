package ru.kpfu.itis.webapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.service.FileService;

import java.util.Set;
import java.util.UUID;

@CrossOrigin
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
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }

            String extension = FilenameUtils.getExtension(originalFilename);
            if (!Set.of("png", "jpg", "jpeg").contains(extension.toLowerCase())) {
                return ResponseEntity.badRequest().body("Unsupported image format");
            }

            String uuid = UUID.randomUUID().toString();
            String fileUid = "events/images/" + uuid + "." + extension;

            String fileUrl = fileService.uploadFile(file, fileUid);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
