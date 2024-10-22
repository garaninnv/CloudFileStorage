package com.garanin.CloudFileStorage.validator.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.stereotype.Component;

@Component
public class FolderForm {
    @NotBlank(message = "Имя папки не должно быть пустым")
    @Pattern(regexp = "^[\\w\\s\\-\\.]+$", message = "Имя папки содержит недопустимые символы")
    private String newNameFolder;

    public @NotBlank(message = "Имя папки не должно быть пустым") @Pattern(regexp = "^[\\w\\s\\-\\.]+$", message = "Имя папки содержит недопустимые символы") String getNewNameFolder() {
        return newNameFolder;
    }

    public void setNewNameFolder(@NotBlank(message = "Имя папки не должно быть пустым") @Pattern(regexp = "^[\\w\\s\\-\\.]+$", message = "Имя папки содержит недопустимые символы") String newNameFolder) {
        this.newNameFolder = newNameFolder;
    }
}