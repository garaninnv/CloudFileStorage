package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.FileFolder;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {

    //Описываем методы работающие с путями к файлам и папкам
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String myBucket;

    public List<FileFolder> listFiles(String bucketName, Long userId) {
        List<FileFolder> fileFolderList = new ArrayList<>();
        try {
            // Получение списка объектов
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix("user-1/")
                            .build());

            // Итерация по результатам
            for (Result<Item> result : results) {
                Item item = result.get(); // Получение элемента s.substring(0, s.length() - 1)
                fileFolderList.
                        add(new FileFolder(item.objectName().endsWith("/") ? item.objectName().substring(0, item.objectName().length() - 1) : item.objectName(),
                                item.objectName(), (item.objectName().endsWith("/")))); // Добавление имени объекта в список
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileFolderList;
    }

    public void delete(String nameFile) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(myBucket)
                            .object(nameFile)
                            .build()
            );
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyFile(String newNameFile, String nameFile) {
        CopySource copySource = CopySource.builder()
                .bucket(myBucket)
                .object(nameFile)
                .build();
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(myBucket)
                            .object(newNameFile)
                            .source(copySource)
                            .build());
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public void createFolder(String newFolder) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(newFolder + "/")
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public void uploadFile(MultipartFile file, String objectName) {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(myBucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(myBucket).build());
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUserBucket (Long userId) {
        // формируем корневой путь для юзера
        return "user-" + userId + "-files";
    }
}
