package com.garanin.CloudFileStorage.dto;

import lombok.Data;


@Data
public class Breadcrumb {
    private String label;
    private String url;

    public Breadcrumb(String label, String url) {
        this.label = label;
        this.url = url;
    }
}
