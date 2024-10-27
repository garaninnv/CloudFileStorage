package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.BreadcrumbDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BreadcrumbService {
    public List<BreadcrumbDTO> getBreadcrumbs(String currentPath) {
        List<BreadcrumbDTO> breadcrumbs = new ArrayList<>();
        String[] parts = currentPath.split("/");
        StringBuilder url = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            url.append(parts[i]).append("/");
            breadcrumbs.add(new BreadcrumbDTO(parts[i], url.toString()));
        }
        return breadcrumbs;
    }
}