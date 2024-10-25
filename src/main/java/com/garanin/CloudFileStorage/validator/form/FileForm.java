package com.garanin.CloudFileStorage.validator.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class FileForm {
    @NotBlank(message = "Имя файла не должно быть пустым")
    @Pattern(regexp = "^[\\w\\s\\-\\.а-яА-ЯёЁ]+$", message = "Имя файла содержит недопустимые символы")
    private String newNameFile;
}
