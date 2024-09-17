package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.Breadcrumb;
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
                if (!item.objectName().equals(path)) {  // Проверка исключает повторное использование для вывода текущей папки
                    fileFolderList
                            .add(new FileFolder(
                                    item.objectName().endsWith("/") ? pathToNameFile(item.objectName().substring(0, item.objectName().length() - 1)) : pathToNameFile(item.objectName()),
                                    //item.objectName().endsWith("/") ? item.objectName().substring(0, item.objectName().length() - 1) : pathToNameFile(item.objectName()),
                                    item.objectName(),
                                    (item.objectName().endsWith("/"))));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileFolderList;
    }

    public void delete(String nameFile, String pathToFile, Long userId, Boolean isFolder) {
        try {
            if (isFolder) {
                Iterable<Result<Item>> results = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(myBucket)
                                .prefix(convertPath(pathToFile, userId) + nameFile + "/")
                                .recursive(true)
                                .build()
                );
                for (Result<io.minio.messages.Item> result : results) {
                    io.minio.messages.Item item = result.get();
                    String oldObjectName = item.objectName();

                    // Удаляем объект
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(myBucket)
                            .object(oldObjectName)
                            .build());
                }
            } else {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(myBucket)
                                .object(convertPath(pathToFile, userId) + nameFile)
                                .build()
                );
            }

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

    public void copyFile(String newNameFile, String nameFile, String pathToFile, Long userId) {
        CopySource copySource = CopySource.builder()
                .bucket(myBucket)
                .object(convertPath(pathToFile, userId) + nameFile)
                .build();
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(myBucket)
                            .object(convertPath(pathToFile, userId) + newNameFile)
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

    // ---------------------------- Переименование папки--------------------------------------
    public void renameFolder(String pathToFolder, String oldFolderName, String newFolderName, Long userId) {
        try {
            String oldPrefix = convertPath(pathToFolder, userId) + oldFolderName + "/";
            String newPrefix = convertPath(pathToFolder, userId) + newFolderName + "/";

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(myBucket)
                            .prefix(oldPrefix)
                            .recursive(true)
                            .build()
            );

            for (Result<io.minio.messages.Item> result : results) {
                io.minio.messages.Item item = result.get();
                String oldObjectName = item.objectName();
                String newObjectName = newPrefix + oldObjectName.substring(oldPrefix.length());

                // Копируем объект с новым именем
                minioClient.copyObject(CopyObjectArgs.builder()
                        .bucket(myBucket)
                        .object(newObjectName)
                        .source(CopySource.builder()
                                .bucket(myBucket)
                                .object(oldObjectName)
                                .build())
                        .build());
                // Удаляем старый объект
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(myBucket)
                        .object(oldObjectName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error renaming folder", e);
        }
    }

    public void createFolder(String path, String newFolder, Long userId) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(convertPath(path, userId) + newFolder + "/")
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

    public void uploadFile(MultipartFile file, String path, String objectName, Long userId) {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(myBucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(myBucket).build());
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(myBucket)
                            .object(convertPath(path, userId) + objectName)
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

    //Формирует правильный путь для загрузки файлов и создания папок
    private String convertPath(String path, Long userId) {
        if (path.lastIndexOf("path=") == -1) {
            return "user-" + userId + "-files/";
        }
        return path.substring(path.lastIndexOf("path=") + 5);
    }

    private String pathToNameFile(String path) {

        return path.substring(path.lastIndexOf("/") + 1);
    }

    public List<FileFolder> search(String searchQuery, Long userId) {
        List<FileFolder> links = new ArrayList<>();
        try {

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(myBucket)
                            .prefix("user-" + userId + "-files")
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                links.add(new FileFolder(convertLebal(result.get().objectName()),
                        pathDoFile(result.get().objectName()),
                        result.get().objectName().endsWith("/")));
            }

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
        return links.stream().filter(el -> el.getName().contains(searchQuery)).toList();
    }

    private String pathDoFile(String s) {
        if (s.endsWith("/")) {
            return s;
        }
        return s.substring(0, s.lastIndexOf("/")) + "/";
    }

    private String convertLebal(String s) {
        String [] ar =   s.split("/");
        return ar[ar.length-1];
    }
}
