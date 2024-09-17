package com.garanin.CloudFileStorage.controllers.MinioControllers;

import com.garanin.CloudFileStorage.dto.Breadcrumb;
import com.garanin.CloudFileStorage.dto.FileFolder;
import com.garanin.CloudFileStorage.services.BreadcrumbService;
import com.garanin.CloudFileStorage.services.MinioService;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MinioController {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioService minioService;

    @Autowired
    private BreadcrumbService breadcrumbService;

    @Value("${minio.bucketName}")
    private String myBucket;

    //Загрузка файлов
    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestHeader(value = "Referer", required = false) String referer) throws UnsupportedEncodingException {
        Long userId = 1L;
        String path = URLDecoder.decode(referer, "UTF-8");
        String objectName = file.getOriginalFilename(); // Имя файла в MinIO
        minioService.uploadFile(file, path, objectName, userId);
        return "redirect:" + referer;
    }

    //Начальная страница и переходы между папками
    @GetMapping("/")
    public String getFiles(@RequestParam(required = false, defaultValue = "/") String path, Model model
//                           ,UserDetails details
    ) {
        Long userId = 1L;
        List<FileFolder> files = minioService.listFiles(path, userId);
        model.addAttribute("files", files);

        model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(path));
        return "allfiles";
    }

    @PostMapping("/newfolder")
    public String newFolder(@RequestParam String newFolder, @RequestHeader(value = "Referer", required = false) String referer) {
        Long userId = 1L;
        try {
            String path = URLDecoder.decode(referer, "UTF-8");
            minioService.createFolder(path, newFolder, userId);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return "redirect:" + referer;
    }

    //Переименование файла
    @PostMapping("/files/update")
    public String updateNameFile(@RequestParam("newNameFile") String newNameFile,
                                 @RequestParam("oldNameFile") String oldNameFile,
                                 @RequestParam("pathToFile") String pathToFile) {
        Long userId = 1L;
        minioService.copyFile(newNameFile, oldNameFile, pathToFile, userId);
        minioService.delete(oldNameFile, pathToFile, userId, false);
        return "redirect:" + pathToFile;
    }

    //Открытие страницы переименования файла
    @GetMapping("/files/update/{nameFile}")
    public String renameFile(@PathVariable("nameFile") String oldNameFile,
                             Model model,
                             @RequestHeader(value = "Referer", required = false) String referer) {
        model.addAttribute("oldNameFile", oldNameFile);
        model.addAttribute("pathToFile", referer);
        return "renameFile";
    }

    //Переименование папки
    @PostMapping("/folders/update")
    public String updateNameFolder(@RequestParam("newFolderName") String newNameFile,
                                   @RequestParam("oldFolderName") String oldNameFile,
                                   @RequestParam("pathToFolder") String pathToFolder) {
        Long userId = 1L;
        try {
            minioService.renameFolder(URLDecoder.decode(pathToFolder, "UTF-8"), oldNameFile, newNameFile, userId);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return "redirect:" + pathToFolder;
    }
    //Открытие страницы переименования папки
    @GetMapping("/folders/update/{nameFolder}")
    public String renameFolder(@PathVariable("nameFolder") String oldFolderName,
                               Model model,
                               @RequestHeader(value = "Referer", required = false) String referer) {
        model.addAttribute("oldFolderName", oldFolderName);
        model.addAttribute("pathToFolder", referer);
        return "renameFolder";
    }

    //Удаление файла
    @DeleteMapping("/files/delete/{nameFile}")
    public String deleteFile(@PathVariable("nameFile") String nameFile,
                             @RequestParam("isFolder") Boolean isFolder,
                             @RequestHeader(value = "Referer", required = false) String referer) {
        Long userId = 1L;
        try {
            minioService.delete(nameFile, URLDecoder.decode(referer, "UTF-8"), userId, isFolder);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return "redirect:" + referer;
    }

    @GetMapping("/search/")
    public String search (@RequestParam("query") String query, Model model) {
        Long userId = 1l;
        List<FileFolder> listLink = new ArrayList<>();
        listLink = minioService.search(query, userId);
        model.addAttribute("listLink", listLink);
        return "search";
    }
}


