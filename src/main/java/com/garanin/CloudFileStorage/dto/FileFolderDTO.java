package com.garanin.CloudFileStorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileFolderDTO {
    private String name;
    private String path;
    private Boolean isFolder;
}
