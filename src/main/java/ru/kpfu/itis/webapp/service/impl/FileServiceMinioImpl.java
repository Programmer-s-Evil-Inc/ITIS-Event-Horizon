package ru.kpfu.itis.webapp.service.impl;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.webapp.service.FileService;

import java.io.InputStream;

@Service
@Slf4j
public class FileServiceMinioImpl implements FileService {

    private final String minioEndpoint;
    private final MinioClient minioClient;
    private final String bucketName;

    public FileServiceMinioImpl(@Value("${minio.bucket}") String bucketName, @Value("${minio.endpoint}") String minioEndpoint, MinioClient minioClient) {
        this.minioEndpoint = minioEndpoint;
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @PostConstruct // Выполняется один раз после инициализации бина
    public void init() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(getPublicPolicy())
                                .build()
                );
                log.info("Minio bucket '{}' создан", bucketName);
            }
        } catch (Exception e) {
            log.error("Ошибка инициализации Minio: ", e);
            throw new ServiceException("Не удалось подключиться к Minio");
        }
    }

    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return getFileUrl(objectName);
    }

    private String getFileUrl(String objectName) {
        return String.format(
                "%s/%s/%s",
                minioEndpoint,
                bucketName,
                objectName
        );
    }

    private String getPublicPolicy() {
        return """
        {
            "Version":"2012-10-17",
            "Statement":[
                {
                    "Effect":"Allow",
                    "Principal":"*",
                    "Action":["s3:GetObject"],
                    "Resource":["arn:aws:s3:::%s/*"]
                }
            ]
        }
        """.formatted(bucketName);
    }
}