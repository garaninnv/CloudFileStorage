package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.Breadcrumb;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BreadcrumbService {
    public List<Breadcrumb> getBreadcrumbs(String currentPath) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();

        // Пример разбивки пути на части
        String[] parts = currentPath.split("/");
        StringBuilder url = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                url.append("/").append(part);
                breadcrumbs.add(new Breadcrumb(part, url.toString()));
            }
        }

        return breadcrumbs;
    }
}
