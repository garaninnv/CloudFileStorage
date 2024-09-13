package com.garanin.CloudFileStorage.controllers.MinioControllers;

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
import java.net.URLEncoder;
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

        String path = URLDecoder.decode(referer.substring(referer.lastIndexOf("path=") + 5), "UTF-8");
        String objectName = file.getOriginalFilename(); // Имя файла в MinIO
        minioService.uploadFile(file,path, objectName);
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
        try {
            String path = URLDecoder.decode(referer.substring(referer.lastIndexOf("path=") + 5), "UTF-8");
            minioService.createFolder(path, newFolder);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return "redirect:" + referer;
    }

    //Переименование файла
    @PostMapping("/files/update")
    public String updateNameFile(@RequestParam("newNameFile") String newNameFile,
                                 @RequestParam("nameFile") String nameFile) {
        minioService.copyFile(newNameFile, nameFile);
        minioService.delete(nameFile);
        return "redirect:/";
    }
    //Открытие страницы переименования файла
    @GetMapping("/files/update/{nameFile}")
    public String renameFile(@PathVariable("nameFile") String nameFile, Model model) {
        model.addAttribute("nameFile", nameFile);
        return "rename";
    }

    //Удаление файла
    @GetMapping("/files/delete/{nameFile}")
    public String deleteFile(@PathVariable("nameFile") String nameFile) {
        minioService.delete(nameFile);
        return "redirect:/";
    }
}


