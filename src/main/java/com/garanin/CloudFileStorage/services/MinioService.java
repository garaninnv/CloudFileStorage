package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.FileFolder;
import com.garanin.CloudFileStorage.repositories.minio.MinioRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {

    private final MinioRepository minioRepository;

    @Autowired
    public MinioService(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public List<FileFolder> listFiles(String path, Long userId) {
        List<FileFolder> fileFolderList = new ArrayList<>();
        Iterable<Result<Item>> results = minioRepository.getListFiles(getUserBucket(userId, path));
        // Итерация по результатам
        for (Result<Item> result : results) {
            Item item = null; // Получение элемента
            try {
                item = result.get();
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
            if (!item.objectName().equals(path)) {  // Проверка исключает повторное использование для вывода текущей папки
                fileFolderList
                        .add(new FileFolder(
                                item.objectName().endsWith("/") ? pathToNameFile(item.objectName().substring(0, item.objectName().length() - 1)) : pathToNameFile(item.objectName()),
                                item.objectName(),
                                (item.objectName().endsWith("/"))));
            }
        }
        return fileFolderList;
    }

    public void delete(String nameFile, String pathToFile, Long userId, Boolean isFolder) {
        if (isFolder) {
            minioRepository.removeFolder(convertPath(pathToFile, userId) + nameFile + "/");
        } else {
            minioRepository.removeFile(convertPath(pathToFile, userId) + nameFile);
        }
    }

    // !!!!!!!Проверить выполнение переименования
    public void copy(String newNameFile, String nameFile, String pathToFile, Long userId) {
        minioRepository.copyObject(convertPath(pathToFile, userId) + nameFile);
    }

    // ---------------------------- Переименование папки--------------------------------------
    public void renameFolder(String pathToFolder, String oldFolderName, String newFolderName, Long userId) {
        String oldPrefix = convertPath(pathToFolder, userId) + oldFolderName + "/";
        String newPrefix = convertPath(pathToFolder, userId) + newFolderName + "/";
        Iterable<Result<Item>> results = minioRepository.doSearch(oldPrefix);

        for (Result<io.minio.messages.Item> result : results) {
            Item item = null;
            try {
                item = result.get();
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
            String oldObjectName = item.objectName();
            String newObjectName = newPrefix + oldObjectName.substring(oldPrefix.length());

            // Копируем объект с новым именем
            minioRepository.copyObjectNewName(oldObjectName, newObjectName);

            // Удаляем старый объект
            minioRepository.removeFolder(oldObjectName);

        }
    }

    public void createFolder(String path, String newFolder, Long userId) {
        minioRepository.newFolder(convertPath(path, userId) + newFolder + "/");
    }

    public void uploadFile(MultipartFile file, String path, String objectName, Long userId) {

        boolean bucketExists = minioRepository.bucketExists();
        if (!bucketExists) {
            minioRepository.makeBucket();
        }
        minioRepository.upFile(convertPath(path, userId) + objectName, file);
    }

    public List<FileFolder> search(String searchQuery, Long userId) {
        List<FileFolder> links = new ArrayList<>();

        Iterable<Result<Item>> results = minioRepository.doSearch("user-" + userId + "-files");

        for (Result<Item> result : results) {
            try {
                links.add(new FileFolder(convertLebal(result.get().objectName()),
                        pathDoFile(result.get().objectName()),
                        result.get().objectName().endsWith("/")));
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
        return links.stream().filter(el -> el.getName().contains(searchQuery)).toList();
    }

    public InputStream downloadFile(String objectName) {
        return minioRepository.download(objectName);
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

    private String pathDoFile(String s) {
        if (s.endsWith("/")) {
            return s;
        }
        return s.substring(0, s.lastIndexOf("/")) + "/";
    }

    private String convertLebal(String s) {
        String[] ar = s.split("/");
        return ar[ar.length - 1];
    }
}
