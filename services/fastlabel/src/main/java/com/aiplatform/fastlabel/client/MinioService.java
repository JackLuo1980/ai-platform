package com.aiplatform.fastlabel.client;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.bucket}")
    private String bucket;

    private final MinioClient minioClient;

    @SneakyThrows
    public String upload(String objectName, InputStream stream, String contentType) {
        ensureBucket();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(stream, -1, 10485760)
                .contentType(contentType)
                .build());
        return objectName;
    }

    @SneakyThrows
    public String upload(String objectName, byte[] data, String contentType) {
        ensureBucket();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                .contentType(contentType)
                .build());
        return objectName;
    }

    @SneakyThrows
    public InputStream download(String objectName) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }

    @SneakyThrows
    public String getPresignedUrl(String objectName) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .method(Method.GET)
                .expiry(1, TimeUnit.HOURS)
                .build());
    }

    @SneakyThrows
    public void delete(String objectName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }

    @SneakyThrows
    private void ensureBucket() {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
