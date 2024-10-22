package com.garanin.CloudFileStorage.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class FolderValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String newFolder = (String) target;
        String regex = "^[\\w\\s\\-\\.]+$";

        if (!Pattern.matches(regex, newFolder)) {
            errors.rejectValue("newFolder", "invalid.newFolder", "Имя папки содержит не допустимые символы");
        }
    }
}
