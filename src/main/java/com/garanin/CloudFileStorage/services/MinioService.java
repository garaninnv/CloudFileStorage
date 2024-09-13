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

    public List<FileFolder> listFiles(String path, Long userId) {
        List<FileFolder> fileFolderList = new ArrayList<>();
        try {
            // Получение списка объектов
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(myBucket)
                            .prefix(getUserBucket(userId, path))
                            .build());

            // Итерация по результатам
            for (Result<Item> result : results) {
                Item item = result.get(); // Получение элемента
                if (!item.objectName().equals(path)){  // Проверка исключает повторное использование для вывода текущей папки
                    fileFolderList
                            .add(new FileFolder(
                                    item.objectName().endsWith("/") ? pathToNameFile(item.objectName().substring(0, item.objectName().length() - 1)) : pathToNameFile(item.objectName()),
                                    item.objectName(),
                                    (item.objectName().endsWith("/"))));
                }
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

    public void createFolder(String path, String newFolder) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(path + newFolder + "/")
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

    public void uploadFile(MultipartFile file,String path, String objectName) {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(myBucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(myBucket).build());
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(path + objectName)
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

    private String getUserBucket(Long userId, String path) {
        // формируем корневой путь для юзера
        if (path.equals("/")) {
            return "user-" + userId + "-files/";
        } else {
            return path;
        }
    }

    private String pathToNameFile(String path) {

        return path.substring(path.lastIndexOf("/") + 1);
    }
}
