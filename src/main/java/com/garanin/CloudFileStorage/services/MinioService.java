package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.FileFolder;
import com.garanin.CloudFileStorage.repositories.minio.MinioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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
        List<String> results = minioRepository.getListFiles(getUserBucket(userId, path));
        for (String result : results) {
            if (!result.equals(getUserBucket(userId, path))) {  // Проверка исключает повторное использование для вывода текущей папки
                fileFolderList
                        .add(new FileFolder(
                                result.endsWith("/") ? pathToNameFile(result.substring(0, result.length() - 1)) : pathToNameFile(result),
                                result.substring(result.indexOf("/") + 1),
                                (result.endsWith("/"))));
            }
        }
        return fileFolderList;
    }

    public void delete(String nameFile, String pathToFile, Long userId, Boolean isFolder) {
        if (isFolder) {
            minioRepository.removeFolder(getUserBucket(userId, pathToFile) + nameFile + "/");
        } else {
            minioRepository.removeFile(getUserBucket(userId, pathToFile) + nameFile);
        }
    }

    //-----------------переименование файла-------------------
    public void copy(String newNameFile, String nameFile, String pathToFile, Long userId) {
        minioRepository.copyObjectNewName(getUserBucket(userId, pathToFile) + nameFile,
                getUserBucket(userId, pathToFile) + newNameFile);
    }

    // ---------------------------- Переименование папки--------------------------------------
    public void renameFolder(String pathToFolder, String oldFolderName, String newFolderName, Long userId) {
        String oldPrefix = getUserBucket(userId, pathToFolder) + oldFolderName + "/";
        String newPrefix = getUserBucket(userId, pathToFolder) + newFolderName + "/";
        List<String> listFolder = minioRepository.doSearch(oldPrefix);
        for (String result : listFolder) {
            String oldObjectName = result;
            String newObjectName = newPrefix + oldObjectName.substring(oldPrefix.length());
            minioRepository.copyObjectNewName(oldObjectName, newObjectName);
        }
        for (String result : listFolder) {
            minioRepository.removeFolder(result);
        }
    }

    public void createFolder(String path, String newFolder, Long userId) {
        minioRepository.newFolder(getUserBucket(userId, path) + newFolder + "/");

    }

    public void uploadFile(MultipartFile file, String path, String objectName, Long userId) {

        boolean bucketExists = minioRepository.bucketExists();
        if (!bucketExists) {
            minioRepository.makeBucket();
        }
        minioRepository.upFile(getUserBucket(userId, path) + objectName, file);
    }

    public List<FileFolder> search(String searchQuery, Long userId) {
        List<FileFolder> links = new ArrayList<>();
        List<String> list = minioRepository.doSearch("user-" + userId + "-files");

        for (String result : list) {
            links.add(new FileFolder(convertLebal(result),
                    getPath(result),
                    result.endsWith("/")));
        }
        return links.stream().filter(el -> el.getName().contains(searchQuery)).toList();
    }

    public InputStream downloadFile(String objectName, Long userId) {
        return minioRepository.download("user-" + userId + "-files/" + objectName);
    }

    private String getPath(String path) {
        int index = path.indexOf("/");
        int end = path.lastIndexOf("/");
        path = path.substring(index, end + 1);
        if (path.length() > 1) {
            return path.substring(1, path.length());
        }
        return path;
    }

    private String getUserBucket(Long userId, String path) {
        // формируем корневой путь для юзера
        if (path.equals("/")) {
            return "user-" + userId + "-files/";
        } else {
            return "user-" + userId + "-files/" + path;
        }
    }

    private String pathToNameFile(String path) {

        return path.substring(path.lastIndexOf("/") + 1);
    }

    private String convertLebal(String s) {
        String[] ar = s.split("/");
        return ar[ar.length - 1];
    }
}