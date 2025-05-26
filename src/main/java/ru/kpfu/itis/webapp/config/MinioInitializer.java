package ru.kpfu.itis.webapp.config;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.entity.Participation;
import ru.kpfu.itis.webapp.repository.ParticipationRepository;
import ru.kpfu.itis.webapp.service.FileService;
import ru.kpfu.itis.webapp.utils.ByteArrayMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.init.enabled", havingValue = "true")
public class MinioInitializer implements ApplicationRunner {
    private final FileService fileService;
    private final ParticipationRepository participationRepository;

    @Override
    public void run(ApplicationArguments args) {
        uploadDefaultImages();
        generateAndUploadTestQRCodes();
    }

    private void uploadDefaultImages() {
        List.of(
                "events/images/event1.jpg",
                "events/images/event2.jpg",
                "ProfilePhoto/avatar1.png",
                "ProfilePhoto/avatar3.jpg"
        ).forEach(this::uploadImage);
    }

    private void uploadImage(String imagePath) {
        try {
            if (fileService.fileExists(imagePath)) return;

            ClassPathResource resource = new ClassPathResource("minio-data/" + imagePath);
            byte[] bytes = resource.getInputStream().readAllBytes();
            MultipartFile file = new ByteArrayMultipartFile(bytes, imagePath);
            fileService.uploadFile(file, imagePath);
            log.info("Uploaded: {}", imagePath);
        } catch (Exception e) {
            log.error("Failed to upload: {}", imagePath, e);
        }
    }

    private void generateAndUploadTestQRCodes() {
        List<Participation> participations = participationRepository.findAll();
        for (Participation participation : participations) {
            String objectName = "subscriptions/qrcodes/" + participation.getId() + ".png";
            if (!fileService.fileExists(objectName)) {
                try {
                    String validationUrl = "http://localhost:8080/api/events/subscriptions/validate?subscriptionId=" + participation.getId();
                    byte[] qrCodeBytes = generateQRCode(validationUrl);

                    MultipartFile qrCodeFile = new ByteArrayMultipartFile(qrCodeBytes, objectName);
                    fileService.uploadFile(qrCodeFile, objectName);

                    log.info("Uploaded QR-code for participation: {}", participation.getId());
                } catch (Exception e) {
                    log.error("Failed to generate QR-code for participation: {}", participation.getId(), e);
                }
            }
        }
    }

    private byte[] generateQRCode(String text) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}