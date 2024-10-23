package com.garanin.CloudFileStorage.validator.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class FileForm {
    @NotBlank(message = "Имя файла не должно быть пустым")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Имя файла содержит недопустимые символы")
    private String newNameFile;

    public @NotBlank(message = "Имя файла не должно быть пустым") @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Имя файла содержит недопустимые символы") String getNewNameFile() {
        return newNameFile;
    }

    public void setNewNameFile(@NotBlank(message = "Имя файла не должно быть пустым") @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Имя файла содержит недопустимые символы") String newNameFile) {
        this.newNameFile = newNameFile;
    }
}
