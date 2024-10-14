package com.garanin.CloudFileStorage.controllers.MinioControllers;

import com.garanin.CloudFileStorage.dto.FileFolder;
import com.garanin.CloudFileStorage.services.BreadcrumbService;
import com.garanin.CloudFileStorage.services.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class MinioController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private BreadcrumbService breadcrumbService;

    //Загрузка файлов
    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam String currentPath) {
        Long userId = 1L;
        String objectName = file.getOriginalFilename();

        minioService.uploadFile(file, currentPath, objectName, userId);
        try {
            return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
        model.addAttribute("currentPath", path);
        return "allfiles";
    }

    @PostMapping("/newfolder")
    public String newFolder(@RequestParam String newFolder,
                            @RequestParam String currentPath) {
        Long userId = 1L;
        minioService.createFolder(currentPath, newFolder, userId);
        try {
            return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    //Переименование файла
    @PostMapping("/files/update")
    public String updateNameFile(@RequestParam("newNameFile") String newNameFile,
                                 @RequestParam("oldNameFile") String oldNameFile,
                                 @RequestParam("currentPath") String currentPath) {
        Long userId = 1L;
        minioService.copy(newNameFile, oldNameFile, currentPath, userId);
        minioService.delete(oldNameFile, currentPath, userId, false);

        try {
            return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    //Открытие страницы переименования файла
    @GetMapping("/files/update/{nameFile}")
    public String renameFile(@PathVariable("nameFile") String oldNameFile,
                             Model model,
                             @RequestParam String currentPath) {
        model.addAttribute("oldNameFile", oldNameFile);
        model.addAttribute("currentPath", currentPath);
        return "renameFile";
    }

    //Переименование папки
    @PostMapping("/folders/update")
    public String updateNameFolder(@RequestParam("newFolderName") String newNameFile,
                                   @RequestParam("oldFolderName") String oldNameFile,
                                   @RequestParam("currentPath") String currentPath) {
        Long userId = 1L;
        try {
            minioService.renameFolder(URLDecoder.decode(currentPath, "UTF-8"), oldNameFile, newNameFile, userId);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    //Открытие страницы переименования папки
    @GetMapping("/folders/update/{nameFolder}")
    public String renameFolder(@PathVariable("nameFolder") String oldFolderName,
                               Model model,
                               @RequestParam(required = false, defaultValue = "/") String currentPath) {
        model.addAttribute("oldFolderName", oldFolderName);
        model.addAttribute("currentPath", currentPath);
        return "renameFolder";
    }

    //Удаление файла
    @DeleteMapping("/files/delete/{nameFile}")
    public String deleteFile(@PathVariable("nameFile") String nameFile,
                             @RequestParam("isFolder") Boolean isFolder,
                             @RequestParam String currentPath) {
        Long userId = 1L;
        minioService.delete(nameFile, currentPath, userId, isFolder);
        try {
            return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/search/")
    public String search(@RequestParam("query") String query, Model model) {
        Long userId = 1l;
        List<FileFolder> listLink;
        listLink = minioService.search(query, userId);
        model.addAttribute("listLink", listLink);
        return "search";
    }

    @GetMapping("/download/")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam String objectName) throws UnsupportedEncodingException {
        InputStream inputStream = minioService.downloadFile(objectName);
        String[] ar = objectName.split("/");
        String nameFile = ar[ar.length - 1];
        InputStreamResource resource = new InputStreamResource(inputStream);
        String decodedObjectName = URLEncoder.encode(nameFile, "UTF-8");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        decodedObjectName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}


