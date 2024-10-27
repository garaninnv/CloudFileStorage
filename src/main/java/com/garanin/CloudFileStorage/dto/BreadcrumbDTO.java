package com.garanin.CloudFileStorage.dto;

import lombok.Data;


@Data
public class BreadcrumbDTO {
    private String label;
    private String url;

    public BreadcrumbDTO(String label, String url) {
        this.label = label;
        this.url = url;
    }
}
