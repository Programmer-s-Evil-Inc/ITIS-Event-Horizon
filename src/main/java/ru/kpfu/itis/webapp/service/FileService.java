package ru.kpfu.itis.webapp.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadFile(MultipartFile multipartFile, String objectName) throws Exception;
    String getBaseUrl();
    boolean fileExists(String objectName);
    void deleteFile(String objectName);
}
