package ru.kpfu.itis.webapp.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.assertj.core.api.BDDAssertions;
import ru.kpfu.itis.webapp.service.impl.FileServiceMinioImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    private FileService fileService;

    private static final String testBucketName = "event-horizon-bucket";

    @BeforeEach
    public void setUp() {
        String testExternalEndpoint = "https://test.domain.com";
        this.fileService = new FileServiceMinioImpl(testBucketName, testExternalEndpoint, minioClient);
    }

    @Test
    void testFileUpload() throws Exception {
        // given
        String contentType = "text/plain";
        byte[] contentBytes = "Test content".getBytes();
        String testFileName = "test.txt";
        MockMultipartFile file = new MockMultipartFile("file", testFileName, contentType, contentBytes);
        ArgumentCaptor<PutObjectArgs> argsCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        when(minioClient.putObject(argsCaptor.capture())).thenReturn(any());

        // when
        String url = fileService.uploadFile(file, testFileName);

        // then
        BDDAssertions.then(url)
                .isNotBlank()
                .contains(testFileName);

        var args = argsCaptor.getValue();
        BDDAssertions.then(args).isNotNull();
        BDDAssertions.then(args.contentType()).isEqualTo(contentType);
        BDDAssertions.then(args.bucket()).isEqualTo(testBucketName);
        BDDAssertions.then(args.object()).isEqualTo(testFileName);
    }
}
