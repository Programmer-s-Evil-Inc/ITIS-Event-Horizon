package ru.kpfu.itis.webapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.service.FileService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Controller", description = "Работа с файлами для событий")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Загрузить файл", description = "Доступно организаторам. Привязка к событию через eventId.")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("eventId") String eventId) {
        try {
            String objectName = "events/" + eventId + "/" + file.getOriginalFilename();
            String fileUrl = fileService.uploadFile(file, objectName);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

}
