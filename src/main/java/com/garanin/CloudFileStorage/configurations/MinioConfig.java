package com.garanin.CloudFileStorage.configurations;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.accessKey}")
    private String minioAccessKey;

    @Value("${minio.secretKey}")
    private String minioSecretKey;

    @Value("${minio.bucketName}")
    private String minioBucketName;

    @Bean (name = "myBucket")
    public String getMinioBucketName() {
        return minioBucketName;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }

    @Bean
    public ApplicationRunner initBucket(MinioClient minioClient) {
        return args -> {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucketName).build());
                System.out.println("Bucket created: " + minioBucketName);
            } else {
                System.out.println("Bucket already exists: " + minioBucketName);
            }
        };
    }
}
