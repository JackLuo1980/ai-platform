package com.aiplatform.lab.common;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String defaultBucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void ensureBucket(String bucket) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure bucket: " + bucket, e);
        }
    }

    public String upload(String bucket, String objectKey, InputStream stream, long size, String contentType) {
        try {
            ensureBucket(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build());
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO: " + objectKey, e);
        }
    }

    public String upload(String objectKey, InputStream stream, long size, String contentType) {
        return upload(defaultBucket, objectKey, stream, size, contentType);
    }

    public InputStream download(String objectKey) {
        return download(defaultBucket, objectKey);
    }

    public InputStream download(String bucket, String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from MinIO: " + objectKey, e);
        }
    }

    public void delete(String objectKey) {
        delete(defaultBucket, objectKey);
    }

    public void delete(String bucket, String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from MinIO: " + objectKey, e);
        }
    }

    public String getPresignedUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL: " + objectKey, e);
        }
    }

    public void copyObject(String sourceKey, String destKey) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(destKey)
                    .source(CopySource.builder()
                            .bucket(defaultBucket)
                            .object(sourceKey)
                            .build())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object: " + sourceKey + " -> " + destKey, e);
        }
    }

    public String getDefaultBucket() {
        return defaultBucket;
    }
}
