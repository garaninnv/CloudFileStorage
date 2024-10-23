package com.garanin.CloudFileStorage.controllers.MinioControllers;

import com.garanin.CloudFileStorage.dto.FileFolder;
import com.garanin.CloudFileStorage.services.BreadcrumbService;
import com.garanin.CloudFileStorage.services.MinioService;
import com.garanin.CloudFileStorage.validator.form.FileForm;
import com.garanin.CloudFileStorage.validator.form.FolderForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
                             Model model,
                             @RequestParam String currentPath) {
        Long userId = 1L;
        String objectName = file.getOriginalFilename();
        if(file.getSize()/1024/1024 < 100) {
            minioService.uploadFile(file, currentPath, objectName, userId);
            try {
                return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        List<FileFolder> files = minioService.listFiles(currentPath, userId);
        model.addAttribute("files", files);
        model.addAttribute("currentPath", currentPath);
        model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(currentPath));
        model.addAttribute("errorMessageMaxSize", "Размер файла превышает 100Мб");
        return "allfiles";
    }

    //Начальная страница и переходы между папками
    @GetMapping("/")
    public String getFiles(@RequestParam(required = false, defaultValue = "/") String path,
                           Model model
    ) {
        Long userId = 1L;
        List<FileFolder> files = minioService.listFiles(path, userId);
        model.addAttribute("files", files);
        model.addAttribute("folderForm", new FolderForm());
        model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(path));
        model.addAttribute("currentPath", path);
        return "allfiles";
    }

    @PostMapping("/newfolder")
    public String newFolder(@Valid @ModelAttribute("newNameFolder") FolderForm folderForm,
                            BindingResult bindingResult,
                            Model model,
                            @RequestParam String currentPath) {
        Long userId = 1L;
        if (bindingResult.hasErrors()) {
            List<FileFolder> files = minioService.listFiles(currentPath, userId);
            model.addAttribute("files", files);
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("currentPath", currentPath);
            model.addAttribute("nameFolder", folderForm.getNewNameFolder());
            model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(currentPath));
            return "allfiles";
        }
        minioService.createFolder(currentPath, folderForm.getNewNameFolder(), userId);
        try {
            return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    //Переименование файла
    @PostMapping("/files/update")
    public String updateNameFile(@Valid @ModelAttribute("newNameFile") FileForm fileForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 @RequestParam("oldNameFile") String oldNameFile,
                                 @RequestParam("currentPath") String currentPath) {
        Long userId = 1L;
        if (!fileForm.getNewNameFile().equals(oldNameFile)) {
            if (bindingResult.hasErrors()) {
                model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
                model.addAttribute("currentPath", currentPath);
                model.addAttribute("newNameFile", fileForm.getNewNameFile().toString());
                model.addAttribute("oldNameFile", oldNameFile);
                model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(currentPath));
                return "renameFile";
            }
            minioService.copy(fileForm.getNewNameFile(), oldNameFile, currentPath, userId);
            minioService.delete(oldNameFile, currentPath, userId, false);
        }
        try {
            return "redirect:/?path=" + URLEncoder.encode(currentPath, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    //Открытие страницы переименования файла
    @GetMapping("/files/update/{newNameFile}")
    public String renameFile(@PathVariable("newNameFile") String newNameFile,
                             Model model,
                             @RequestParam String currentPath) {
        model.addAttribute("oldNameFile", newNameFile);
        model.addAttribute("currentPath", currentPath);
        return "renameFile";
    }

    //Переименование папки
    @PostMapping("/folders/update")
    public String updateNameFolder(@Valid @ModelAttribute("newNameFolder") FolderForm folderForm,
                                   BindingResult bindingResult,
                                   Model model,
                                   // @RequestParam("newFolderName") String newNameFile,
                                   @RequestParam("oldFolderName") String oldFolderName,
                                   @RequestParam("currentPath") String currentPath) {
        Long userId = 1L;
        if (!folderForm.getNewNameFolder().equals(oldFolderName)) {
            if (bindingResult.hasErrors()) {
                model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
                model.addAttribute("currentPath", currentPath);
                model.addAttribute("newNameFolder", folderForm.getNewNameFolder().toString());
                model.addAttribute("oldFolderName", oldFolderName);
                model.addAttribute("breadcrumbs", breadcrumbService.getBreadcrumbs(currentPath));
                return "renameFolder";
            }
            try {
                minioService.renameFolder(URLDecoder.decode(currentPath, "UTF-8"), oldFolderName, folderForm.getNewNameFolder(), userId);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

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
        model.addAttribute("newNameFolder", oldFolderName);
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


