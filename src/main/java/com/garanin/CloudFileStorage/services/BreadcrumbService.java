package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.Breadcrumb;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BreadcrumbService {
    public List<Breadcrumb> getBreadcrumbs(String currentPath) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        Long userId = 1L;
        // Пример разбивки пути на части
        String[] parts = currentPath.split("/");
        StringBuilder url = new StringBuilder();

        url.append("user-" + userId + "-files/");
        for (int i = 1; i < parts.length; i++) {
            url.append(parts[i]).append("/");
            breadcrumbs.add(new Breadcrumb(parts[i], url.toString()));
        }

        return breadcrumbs;
    }
}
