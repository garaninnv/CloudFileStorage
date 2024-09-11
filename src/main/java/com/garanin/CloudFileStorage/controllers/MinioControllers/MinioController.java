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

import java.util.List;

@Controller
@RequestMapping("/")
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
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        String objectName = file.getOriginalFilename(); // Имя файла в MinIO
        minioService.uploadFile(file, objectName);
        return "redirect:/";
    }

    //Начальная страница
    @GetMapping()
    public String getFiles(Model model
//            , UserDetails details
    ) {
        Long userId = 1L;
        //удалить myBucket из контроллера
        List<FileFolder> files = minioService.listFiles(myBucket, userId);
        model.addAttribute("files", files);

        String currentPath = "/user-1/"; // Текущий путь
        model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(currentPath));
        return "allfiles";
    }

    //Для перехода в папку. Нужна доработка
    @GetMapping("/{path}")
    public String getFiles(@PathVariable("path") String path, Model model) {
        Long userId = 1L;
        List<FileFolder> files = minioService.listFiles(myBucket, userId);
        model.addAttribute("files", files);

        String currentPath = "/"; // Текущий путь
        model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(currentPath));
        return "allfiles";
    }

    @PostMapping("/newfolder")
    public String newFolder(@RequestParam String newFolder) {
        minioService.createFolder(newFolder);
        return "redirect:/";
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


