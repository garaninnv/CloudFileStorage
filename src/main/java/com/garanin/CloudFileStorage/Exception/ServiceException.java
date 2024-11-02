package com.garanin.CloudFileStorage.Exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class ServiceException{

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public RedirectView handleMaxSizeException(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessageMaxSize", "Превышен максимальный размер загрузки 100Мб");
       return new RedirectView("/?path=/");
    }
}