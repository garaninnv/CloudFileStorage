package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.repositories.minio.MinioRepository;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.MinioProperties;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
class MinioServiceTest {

//    private final MinioClient minioClient = MinioClient.builder()
//            .endpoint("http://localhost:9000")
//            .credentials("minioadmin", "minioadmin")
//            .build();;
//
//    private final String myBucket = "my-bucket";
//
//    @Test
//    void createFolder() {
//        final MinioRepository minioRepository = new MinioRepository(minioClient, myBucket);
//        final MinioService minioService = new MinioService(minioRepository);
//        String path = "http://localhost:8080/?path=user-1-files/folder1/folder2/";
//        String nameFolder = "folder3";
//        Long userId = 1L;
//        minioService.createFolder(path, nameFolder, userId);
//        //------
//        String pathForTest = "user-1-files/folder1/folder2/folder3/";
//        List<String> folders = new ArrayList<>();
//        try {
//            Iterable<Result<Item>> results;
//            results = minioClient.listObjects(ListObjectsArgs.builder()
//                    .bucket(myBucket)
//                    .prefix(pathForTest)
//                    .build());
//            Item item = null;
//            for (Result<Item> result : results) {
//                item = result.get();
//                folders.add(item.objectName());
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        //-----
//        assertEquals(folders.size(), 1);
//        assertEquals(folders.get(0), pathForTest);
//    }
}