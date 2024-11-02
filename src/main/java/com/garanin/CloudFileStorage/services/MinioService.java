package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.FileFolderDTO;
import com.garanin.CloudFileStorage.repositories.minio.MinioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MinioService {

    private final MinioRepository minioRepository;

    @Autowired
    public MinioService(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public List<FileFolderDTO> listFiles(String path, Long userId) {
        List<FileFolderDTO> fileFolderList = new ArrayList<>();
        List<String> results = minioRepository.getListFiles(getUserBucket(userId, path));
        for (String result : results) {
            if (!result.equals(getUserBucket(userId, path))) {  // Проверка исключает повторное использование для вывода текущей папки
                fileFolderList
                        .add(new FileFolderDTO(
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

    public void uploadFiles(MultipartFile[] folderfiles, String path, Long userId) {
        boolean bucketExists = minioRepository.bucketExists();
        if (!bucketExists) {
            minioRepository.makeBucket();
        }
        for (MultipartFile file : folderfiles) {
           String fileName = file.getOriginalFilename();
            minioRepository.upFile(getUserBucket(userId, path) + fileName, file);
        }
    }

    public List<FileFolderDTO> search(String searchQuery, Long userId) {
        List<FileFolderDTO> links = new ArrayList<>();
        List<String> list = minioRepository.doSearch("user-" + userId + "-files");

        for (String result : list) {
            links.add(new FileFolderDTO(convertLebal(result),
                    getPath(result),
                    result.endsWith("/")));
        }
        return links.stream().filter(el -> el.getName().toLowerCase().contains(searchQuery.toLowerCase())).toList();
    }

    public InputStream downloadFile(String objectName, Long userId) {
        return minioRepository.download("user-" + userId + "-files/" + objectName);
    }

    public byte[] downloadFiles(String objectName, Long userId, String[] name) throws Exception{
        List<String> list = minioRepository.getRecursiveListFiles("user-" + userId + "-files/" + objectName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (String element : list) {
            try (InputStream fileStream = minioRepository.download(element)) {
                int l = ("user-" + userId + "-files/" + objectName).length() - name[name.length - 1].length();
                zos.putNextEntry(new ZipEntry(element.substring(l-1)));

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileStream.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
        zos.close();
        return baos.toByteArray();
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