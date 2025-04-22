package ru.kpfu.itis.webapp.service;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MinioServiceTest {

    @Autowired
    private MinioService minioService;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final String testFileName = "test.txt";

    @Test
    void testFileUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                testFileName,
                "text/plain",
                "Test content".getBytes()
        );

        String url = minioService.uploadFile(file, testFileName);

        assertNotNull(url, "URL файла не должен быть null");
        assertTrue(url.contains(testFileName), "URL должен содержать имя файла");

        boolean exists = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(testFileName)
                        .build()
        ) != null;
        assertTrue(exists, "Файл должен существовать в MinIO");
    }

    @AfterEach
    void tearDown() {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(testFileName)
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Ошибка при очистке тестового файла: " + e.getMessage());
        }
    }
}
