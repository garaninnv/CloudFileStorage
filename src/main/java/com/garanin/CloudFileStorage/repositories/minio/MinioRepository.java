package com.garanin.CloudFileStorage.repositories.minio;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MinioRepository {
    private final MinioClient minioClient;

    private final String myBucket;

    public List<String> getListFiles(String prefix) {
        Iterable<Result<Item>> results = new ArrayList<>();
        List<String> files = new ArrayList<>();
        try {
            // Получение списка объектов
            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(myBucket)
                            .prefix(prefix)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Result<Item> result : results) {
            try {
                files.add(result.get().objectName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return files;
    }

    public void removeFolder(String prefix) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(myBucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );
        for (Result<io.minio.messages.Item> result : results) {
            Item item = null;
            try {
                item = result.get();
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
            String oldObjectName = item.objectName();

            // Удаляем объект
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(myBucket)
                        .object(oldObjectName)
                        .build());
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void removeFile(String p) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(myBucket)
                            .object(p)
                            .build()
            );
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void copyObjectNewName(String oldObjectName, String newObjectName) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .source(CopySource.builder()
                            .bucket(myBucket)
                            .object(oldObjectName)
                            .build())
                    .bucket(myBucket)
                    .object(newObjectName)
                    .build());
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void newFolder(String obj) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(obj)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean bucketExists() {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(myBucket).build());
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void makeBucket() {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(myBucket).build());
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void upFile(String path, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> doSearch(String pr) {
        List<String> list = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(myBucket)
                        .prefix(pr)
                        .recursive(true)
                        .build()
        );
        for (Result<io.minio.messages.Item> result : results) {
            try {
                list.add(result.get().objectName());
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    public InputStream download(String objectName) {

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(myBucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }
}