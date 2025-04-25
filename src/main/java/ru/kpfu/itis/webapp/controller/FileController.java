package ru.kpfu.itis.webapp.controller;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.service.impl.FileServiceMinioImpl;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileServiceMinioImpl minioService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("eventId") String eventId) {
        try {
            String objectName = "events/" + eventId + "/" + file.getOriginalFilename();
            String fileUrl = minioService.uploadFile(file, objectName);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }
}
