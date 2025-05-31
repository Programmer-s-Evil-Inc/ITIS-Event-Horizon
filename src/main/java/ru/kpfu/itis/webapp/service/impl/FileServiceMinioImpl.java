package ru.kpfu.itis.webapp.service.impl;

import io.minio.*;
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
        log.info("Initializing Minio storage [Bucket: {}, Endpoint: {}]", this.bucketName, this.minioEndpoint);
        initializeBucket();
    }

    private void initializeBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                log.info("Creating bucket: {}", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

                log.debug("Setting public policy for bucket: {}", bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(getPublicPolicy())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Error initializing Minio bucket", e);
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

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error deleting file: {}", objectName, e);
            throw new ServiceException("File deletion failed");
        }
    }

    @Override
    public String getBaseUrl() {
        return minioEndpoint + "/" + bucketName + "/";
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
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